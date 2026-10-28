package com.barberorganizer.barberorganizer.View;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.barberorganizer.R;
import com.barberorganizer.barberorganizer.Model.Servico;
import com.barberorganizer.barberorganizer.Model.Usuario;
import com.barberorganizer.barberorganizer.Repository.AdminRepository;
import java.util.List;

/**
 * Listas do painel Admin.
 * Barbeiros e serviços: cadastro e exclusão (exclusão lógica, o histórico de agendamentos é mantido).
 * Clientes: somente leitura.
 */
public class AdminListaFragment extends Fragment {
    private static final String ARG_TIPO = "tipo_admin";
    private static final int VERMELHO = 0xFFFF8585;
    private static final int VERMELHO_BOTAO = 0xFFE5534B;
    private static final int CINZA_BOTAO = 0xFF454351;
    private String tipo;
    private LinearLayout conteudo;
    private int branco, cinza, ouro;

    public static AdminListaFragment novaInstancia(String tipo) {
        AdminListaFragment f = new AdminListaFragment(); Bundle b = new Bundle(); b.putString(ARG_TIPO,tipo); f.setArguments(b); return f;
    }

    @Nullable @Override public View onCreateView(@NonNull android.view.LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        tipo = getArguments() == null ? "barbeiros" : getArguments().getString(ARG_TIPO,"barbeiros");
        Context c=requireContext(); branco=c.getColor(R.color.text_white); cinza=c.getColor(R.color.text_gray); ouro=c.getColor(R.color.gold_accent);
        ScrollView scroll=new ScrollView(c); scroll.setBackgroundColor(c.getColor(R.color.bg_dark));
        LinearLayout body=new LinearLayout(c); body.setOrientation(LinearLayout.VERTICAL); body.setPadding(dp(20),dp(20),dp(20),dp(20)); scroll.addView(body);
        TextView voltar=texto("‹  Voltar ao painel",14,ouro,true); voltar.setPadding(0,0,0,dp(16)); voltar.setClickable(true); voltar.setOnClickListener(v -> ((AdminActivity)requireActivity()).mostrarPainel()); body.addView(voltar);
        String titulo = tipo.equals("servicos") ? "Serviços cadastrados" : tipo.equals("clientes") ? "Clientes cadastrados" : "Barbeiros cadastrados";
        body.addView(texto(titulo,22,branco,true));
        if(tipo.equals("barbeiros") || tipo.equals("servicos")) {
            final boolean ehServico = tipo.equals("servicos");
            TextView novo=botao(ehServico ? "+  Novo serviço" : "+  Novo barbeiro"); LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,dp(46));bp.topMargin=dp(14);body.addView(novo,bp);
            novo.setOnClickListener(v -> {
                if (ehServico) abrirFormServico();
                else ((AdminActivity)requireActivity()).abrirCadastroBarbeiro();
            });
        }
        conteudo=new LinearLayout(c); conteudo.setOrientation(LinearLayout.VERTICAL); LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,-2);cp.topMargin=dp(16);body.addView(conteudo,cp);
        conteudo.addView(texto("Carregando...",13,cinza,false));
        return scroll;
    }

    @Override public void onResume() {
        super.onResume();
        if (conteudo != null) carregar();
    }

    private AdminRepository repo() { return new AdminRepository(requireContext()); }

    private void carregar(){
        AdminRepository repo=repo();
        if(tipo.equals("servicos")) repo.listarServicos(new AdminRepository.CallbackDados<List<Servico>>() {
            @Override public void sucesso(List<Servico> dados){renderServicos(dados);}
            @Override public void erro(String m){renderErro(m);}
        });
        else if(tipo.equals("clientes")) repo.listarClientes(new AdminRepository.CallbackDados<List<Usuario>>() {
            @Override public void sucesso(List<Usuario> dados){renderUsuarios(dados,"Nenhum cliente cadastrado.",false);}
            @Override public void erro(String m){renderErro(m);}
        });
        else repo.listarBarbeiros(new AdminRepository.CallbackDados<List<Usuario>>() {
                @Override public void sucesso(List<Usuario> dados){renderUsuarios(dados,"Nenhum barbeiro cadastrado.",true);}
                @Override public void erro(String m){renderErro(m);}
            });
    }

    private void renderServicos(List<Servico> dados){
        if(!isAdded()||conteudo==null)return;
        conteudo.removeAllViews();
        if(dados==null||dados.isEmpty()){conteudo.addView(texto("Nenhum serviço cadastrado.",13,cinza,false));return;}
        for(Servico s:dados){
            String nome=s.getNome()==null?"Serviço":s.getNome();
            String detalhe=String.format(java.util.Locale.forLanguageTag("pt-BR"),"R$ %.2f • %d min",s.getPreco(),s.getDuracao()).replace('.',',');
            conteudo.addView(item(nome,detalhe,() -> pedirExclusaoServico(s)));
        }
    }

    private void renderUsuarios(List<Usuario> dados,String vazio,boolean podeExcluir){
        if(!isAdded()||conteudo==null)return;
        conteudo.removeAllViews();
        if(dados==null||dados.isEmpty()){conteudo.addView(texto(vazio,13,cinza,false));return;}
        for(Usuario u:dados){
            String nome=u.getNome()==null?"Usuário":u.getNome();
            String detalhe=u.getEmail()==null?"":u.getEmail();
            if(u.getTelefone()!=null&&!u.getTelefone().trim().isEmpty())detalhe += (detalhe.isEmpty()?"":"\n")+u.getTelefone();
            if(podeExcluir) conteudo.addView(item(nome,detalhe,() -> pedirExclusaoBarbeiro(u)));
            else conteudo.addView(item(nome,detalhe,null));
        }
    }

    private void renderErro(String mensagem){if(!isAdded()||conteudo==null)return;conteudo.removeAllViews();conteudo.addView(texto(mensagem,13,VERMELHO,false));}

    // =====================================================
    // EXCLUSÃO (lógica: ativo = false)
    // =====================================================

    private void pedirExclusaoBarbeiro(Usuario u){
        String nome=u.getNome()==null?"este barbeiro":u.getNome();
        confirmarExclusao("Excluir barbeiro",
                "Excluir "+nome+"? Ele deixa de aparecer na lista e no agendamento. O histórico de agendamentos é mantido.",
                () -> repo().excluirBarbeiro(u.getId(), aposAcao("Barbeiro excluído.")));
    }

    private void pedirExclusaoServico(Servico s){
        String nome=s.getNome()==null?"este serviço":s.getNome();
        confirmarExclusao("Excluir serviço",
                "Excluir "+nome+"? Ele deixa de aparecer no catálogo e no agendamento. O histórico de agendamentos é mantido.",
                () -> repo().excluirServico(s.getId(), aposAcao("Serviço excluído.")));
    }

    private AdminRepository.CallbackDados<Boolean> aposAcao(String mensagemSucesso){
        return new AdminRepository.CallbackDados<Boolean>() {
            @Override public void sucesso(Boolean ok){ if(!isAdded())return; avisar(mensagemSucesso); carregar(); }
            @Override public void erro(String m){ if(!isAdded())return; avisar(m); }
        };
    }

    private void avisar(String mensagem){ Toast.makeText(requireContext(),mensagem,Toast.LENGTH_LONG).show(); }

    // =====================================================
    // DIÁLOGOS (visual próprio, no padrão escuro do app)
    // =====================================================

    private void confirmarExclusao(String titulo,String mensagem,Runnable aoConfirmar){
        LinearLayout box=caixaDialogo();
        box.addView(texto(titulo,17,branco,true));
        TextView msg=texto(mensagem,13,0xFFB8B8C4,false); msg.setPadding(0,dp(10),0,dp(18)); box.addView(msg);
        TextView cancelar=botaoDialogo("Cancelar",CINZA_BOTAO,branco);
        TextView excluir=botaoDialogo("Excluir",VERMELHO_BOTAO,Color.WHITE);
        box.addView(linhaBotoes(cancelar,excluir));
        AlertDialog dialogo=abrirDialogo(box);
        cancelar.setOnClickListener(v -> dialogo.dismiss());
        excluir.setOnClickListener(v -> { dialogo.dismiss(); aoConfirmar.run(); });
    }

    private void abrirFormServico(){
        LinearLayout box=caixaDialogo();
        box.addView(texto("Novo serviço",17,branco,true));
        EditText edtNome=campo("Nome do serviço",InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        EditText edtPreco=campo("Preço em R$ (ex.: 35,00)",InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        EditText edtDuracao=campo("Duração em minutos (ex.: 30)",InputType.TYPE_CLASS_NUMBER);
        box.addView(edtNome,margemTopo(14));
        box.addView(edtPreco,margemTopo(10));
        box.addView(edtDuracao,margemTopo(10));
        TextView erro=texto("",12,VERMELHO,false); erro.setVisibility(View.GONE); box.addView(erro,margemTopo(10));
        TextView cancelar=botaoDialogo("Cancelar",CINZA_BOTAO,branco);
        TextView salvar=botaoDialogo("Salvar",ouro,0xFF1F2033);
        box.addView(linhaBotoes(cancelar,salvar),margemTopo(16));
        AlertDialog dialogo=abrirDialogo(box);
        cancelar.setOnClickListener(v -> dialogo.dismiss());
        salvar.setOnClickListener(v -> {
            String nome=edtNome.getText().toString().trim();
            double preco=lerPreco(edtPreco.getText().toString());
            int duracao=lerInteiro(edtDuracao.getText().toString());
            if(nome.isEmpty()){mostrarErro(erro,"Informe o nome do serviço.");edtNome.requestFocus();return;}
            if(!(preco>0)){mostrarErro(erro,"Informe um preço válido, maior que zero.");edtPreco.requestFocus();return;}
            if(duracao<=0){mostrarErro(erro,"Informe a duração em minutos, maior que zero.");edtDuracao.requestFocus();return;}
            double precoFinal=Math.round(preco*100)/100.0;
            salvar.setEnabled(false); erro.setVisibility(View.GONE);
            repo().criarServico(nome,precoFinal,duracao,new AdminRepository.CallbackDados<Boolean>() {
                @Override public void sucesso(Boolean ok){
                    if(!isAdded())return;
                    dialogo.dismiss(); avisar("Serviço cadastrado."); carregar();
                }
                @Override public void erro(String m){
                    if(!isAdded())return;
                    salvar.setEnabled(true); mostrarErro(erro,m);
                }
            });
        });
    }

    private void mostrarErro(TextView alvo,String mensagem){alvo.setText(mensagem);alvo.setVisibility(View.VISIBLE);}
    private static double lerPreco(String s){try{return Double.parseDouble(s.trim().replace(',','.'));}catch(NumberFormatException e){return -1;}}
    private static int lerInteiro(String s){try{return Integer.parseInt(s.trim());}catch(NumberFormatException e){return -1;}}

    private AlertDialog abrirDialogo(View conteudoDialogo){
        AlertDialog d=new AlertDialog.Builder(requireContext()).setView(conteudoDialogo).create();
        if(d.getWindow()!=null) d.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        d.show();
        return d;
    }

    private LinearLayout caixaDialogo(){
        LinearLayout box=caixa(requireContext().getColor(R.color.card_background));
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(20),dp(20),dp(20),dp(16));
        return box;
    }

    private LinearLayout linhaBotoes(TextView esquerda,TextView direita){
        LinearLayout linha=new LinearLayout(requireContext());
        linha.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams pe=new LinearLayout.LayoutParams(0,dp(44),1);
        LinearLayout.LayoutParams pd=new LinearLayout.LayoutParams(0,dp(44),1); pd.leftMargin=dp(10);
        linha.addView(esquerda,pe); linha.addView(direita,pd);
        return linha;
    }

    private TextView botaoDialogo(String label,int fundo,int corTexto){
        TextView t=texto(label,14,corTexto,true); t.setGravity(Gravity.CENTER);
        GradientDrawable d=new GradientDrawable(); d.setColor(fundo); d.setCornerRadius(dp(12));
        t.setBackground(d); t.setClickable(true); t.setFocusable(true);
        return t;
    }

    private EditText campo(String dica,int tipoEntrada){
        EditText e=new EditText(requireContext());
        e.setHint(dica); e.setHintTextColor(cinza); e.setTextColor(branco); e.setTextSize(14);
        e.setInputType(tipoEntrada); e.setSingleLine(true);
        e.setPadding(dp(14),dp(12),dp(14),dp(12));
        GradientDrawable d=new GradientDrawable(); d.setColor(requireContext().getColor(R.color.input_background)); d.setCornerRadius(dp(12));
        e.setBackground(d);
        return e;
    }

    private LinearLayout.LayoutParams margemTopo(int topoDp){
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2); p.topMargin=dp(topoDp); return p;
    }

    // =====================================================
    // COMPONENTES VISUAIS
    // =====================================================

    /** Cartão da lista. Se aoExcluir não for nulo, mostra o botão "Excluir" à direita. */
    private View item(String titulo,String sub,@Nullable Runnable aoExcluir){
        LinearLayout card=caixa(requireContext().getColor(R.color.card_background));
        card.setOrientation(LinearLayout.HORIZONTAL); card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(14),dp(13),dp(14),dp(13));
        LinearLayout textos=new LinearLayout(requireContext()); textos.setOrientation(LinearLayout.VERTICAL);
        textos.addView(texto(titulo,15,branco,true));
        if(sub!=null&&!sub.isEmpty()){
            TextView b=texto(sub,12,cinza,false);
            LinearLayout.LayoutParams pb=new LinearLayout.LayoutParams(-1,-2); pb.topMargin=dp(5);
            textos.addView(b,pb);
        }
        card.addView(textos,new LinearLayout.LayoutParams(0,-2,1));
        if(aoExcluir!=null){
            TextView excluir=texto("Excluir",12,VERMELHO,true);
            excluir.setPadding(dp(12),dp(8),dp(4),dp(8));
            excluir.setClickable(true); excluir.setFocusable(true);
            excluir.setOnClickListener(v -> aoExcluir.run());
            card.addView(excluir);
        }
        LinearLayout.LayoutParams pc=new LinearLayout.LayoutParams(-1,-2); pc.bottomMargin=dp(9);
        card.setLayoutParams(pc);
        return card;
    }

    private TextView botao(String label){TextView t=texto(label,14,0xFF1F2033,true);t.setGravity(Gravity.CENTER);GradientDrawable d=new GradientDrawable();d.setColor(ouro);d.setCornerRadius(dp(14));t.setBackground(d);t.setClickable(true);t.setFocusable(true);return t;}
    private TextView texto(String s,int size,int color,boolean bold){TextView t=new TextView(requireContext());t.setText(s);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(null,Typeface.BOLD);return t;}
    private LinearLayout caixa(int color){LinearLayout l=new LinearLayout(requireContext());GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(14));l.setBackground(d);return l;}
    private int dp(float n){return (int)(n*getResources().getDisplayMetrics().density+0.5f);}
}