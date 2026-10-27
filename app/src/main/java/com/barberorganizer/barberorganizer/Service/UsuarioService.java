package com.barberorganizer.barberorganizer.Service;

import android.content.Context;
import android.util.Patterns;

import com.barberorganizer.barberorganizer.Core.Network.RepassadorCallback;
import com.barberorganizer.barberorganizer.Core.Network.ResultadoCallback;
import com.barberorganizer.barberorganizer.Core.Session.SessaoManager;
import com.barberorganizer.barberorganizer.Model.CadastroRequest;
import com.barberorganizer.barberorganizer.Model.CriarPerfilAdminRequest;
import com.barberorganizer.barberorganizer.Model.CriarPerfilClienteRequest;
import com.barberorganizer.barberorganizer.Model.LoginRequest;
import com.barberorganizer.barberorganizer.Model.LoginResponse;
import com.barberorganizer.barberorganizer.Model.Resultado;
import com.barberorganizer.barberorganizer.Model.SupabaseUser;
import com.barberorganizer.barberorganizer.Repository.UsuarioRepository;

public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final SessaoManager sessaoManager;

    public UsuarioService(Context context) {
        usuarioRepository = new UsuarioRepository(context);
        sessaoManager = new SessaoManager(
                context.getApplicationContext()
        );
    }

    // =====================================================
    // LOGIN: autentica e confirma o perfil no Supabase.
    // =====================================================

    public Resultado login(
            LoginRequest request,
            ResultadoCallback<String> callback
    ) {
        if (request == null) {
            return Resultado.erro(
                    Resultado.Campo.NENHUM,
                    "Informe o e-mail e a senha."
            );
        }

        Resultado validacao = validarCredenciais(
                request.getEmail(),
                request.getPassword()
        );

        if (!validacao.isSucesso()) {
            return validacao;
        }

        usuarioRepository.login(
                request,
                new RepassadorCallback<LoginResponse, String>(callback) {

                    @Override
                    public void onSucesso(LoginResponse resposta) {
                        if (!salvarSessaoSeHouver(resposta)) {
                            sessaoManager.limparSessao();
                            destino.onErro(
                                    "O Supabase não retornou uma sessão válida."
                            );
                            return;
                        }

                        usuarioRepository.obterMeuPerfil(
                                new RepassadorCallback<String, String>(destino) {

                                    @Override
                                    public void onSucesso(String perfilRecebido) {
                                        String perfil = normalizarPerfil(
                                                perfilRecebido
                                        );

                                        if (!perfilValido(perfil)) {
                                            sessaoManager.limparSessao();
                                            destino.onErro(
                                                    "O perfil da conta não foi reconhecido. "
                                                            + "Verifique o cadastro no banco."
                                            );
                                            return;
                                        }

                                        sessaoManager.salvarPerfilUsuario(perfil);
                                        destino.onSucesso(perfil);
                                    }

                                    @Override
                                    public void onErro(String mensagem) {
                                        sessaoManager.limparSessao();
                                        destino.onErro(
                                                "Login realizado, mas não foi possível "
                                                        + "confirmar o perfil. " + mensagem
                                        );
                                    }

                                    @Override
                                    public void onSessaoExpirada() {
                                        sessaoManager.limparSessao();
                                        destino.onSessaoExpirada();
                                    }
                                }
                        );
                    }

                    @Override
                    public void onErro(String mensagem) {
                        destino.onErro(mensagem);
                    }

                    @Override
                    public void onSessaoExpirada() {
                        sessaoManager.limparSessao();
                        destino.onSessaoExpirada();
                    }
                }
        );

        return Resultado.ok();
    }

    // =====================================================
    // CADASTRO PÚBLICO: sempre cria Cliente.
    // =====================================================

    public Resultado cadastrar(
            CadastroRequest request,
            ResultadoCallback<Boolean> callback
    ) {
        if (request == null) {
            return Resultado.erro(
                    Resultado.Campo.NENHUM,
                    "Os dados do cadastro não foram informados."
            );
        }

        Resultado validacao = validarCadastro(
                request.getNome(),
                request.getTelefone(),
                request.getEmail(),
                request.getPassword()
        );

        if (!validacao.isSucesso()) {
            return validacao;
        }

        usuarioRepository.cadastrar(
                request,
                new RepassadorCallback<LoginResponse, Boolean>(callback) {

                    @Override
                    public void onSucesso(LoginResponse resposta) {
                        if (!salvarSessaoSeHouver(resposta)) {
                            sessaoManager.limparSessao();
                            destino.onErro(
                                    "A conta foi criada, mas o Supabase não retornou "
                                            + "uma sessão. Confira se a confirmação de "
                                            + "e-mail está desativada."
                            );
                            return;
                        }

                        SupabaseUser user = resposta.getUser();

                        String emailAuth = user.getEmail() == null
                                ? request.getEmail().trim()
                                : user.getEmail().trim();

                        CriarPerfilClienteRequest perfilRequest =
                                new CriarPerfilClienteRequest(
                                        user.getId(),
                                        request.getNome().trim(),
                                        emailAuth,
                                        request.getTelefone().trim()
                                );

                        usuarioRepository.criarPerfilCliente(
                                perfilRequest,
                                new RepassadorCallback<Void, Boolean>(destino) {

                                    @Override
                                    public void onSucesso(Void ignorado) {
                                        // O cadastro público não mantém a
                                        // sessão do novo Cliente aberta.
                                        sessaoManager.limparSessao();
                                        destino.onSucesso(Boolean.TRUE);
                                    }

                                    @Override
                                    public void onErro(String mensagem) {
                                        sessaoManager.limparSessao();
                                        destino.onErro(
                                                "A conta existe no Supabase Auth, mas "
                                                        + "o perfil Cliente não foi concluído. "
                                                        + mensagem
                                                        + " Verifique o banco antes de repetir."
                                        );
                                    }

                                    @Override
                                    public void onSessaoExpirada() {
                                        sessaoManager.limparSessao();
                                        destino.onErro(
                                                "A sessão expirou antes de concluir o perfil. "
                                                        + "A conta pode já existir no Auth."
                                        );
                                    }
                                }
                        );
                    }

                    @Override
                    public void onErro(String mensagem) {
                        destino.onErro(mensagem);
                    }

                    @Override
                    public void onSessaoExpirada() {
                        destino.onSessaoExpirada();
                    }
                }
        );

        return Resultado.ok();
    }

    // =====================================================
    // CADASTRO ADMINISTRATIVO:
    // cria Cliente, Barbeiro ou Administrador.
    // Não troca o token do Admin pelo token da conta nova.
    // =====================================================

    public Resultado cadastrarPeloAdministrador(
            String nome,
            String telefone,
            String email,
            String senha,
            String perfil,
            ResultadoCallback<Boolean> callback
    ) {
        Resultado validacao = validarCadastro(
                nome,
                telefone,
                email,
                senha
        );

        if (!validacao.isSucesso()) {
            return validacao;
        }

        String perfilEscolhido = normalizarPerfil(perfil);

        if (!perfilValido(perfilEscolhido)) {
            return Resultado.erro(
                    Resultado.Campo.NENHUM,
                    "Selecione Cliente, Barbeiro ou Administrador."
            );
        }

        if (!sessaoManager.estaLogado()
                || !"ADMIN".equals(sessaoManager.getPerfilUsuario())) {
            return Resultado.erro(
                    Resultado.Campo.NENHUM,
                    "Somente um Administrador autenticado pode cadastrar usuários."
            );
        }

        CadastroRequest request = new CadastroRequest(
                email.trim(),
                senha,
                nome.trim(),
                telefone.trim()
        );

        // Signup é uma rota pública. Não salvamos a sessão retornada
        // porque ela pertence à conta recém-criada, não ao Admin.
        usuarioRepository.cadastrar(
                request,
                new RepassadorCallback<LoginResponse, Boolean>(callback) {

                    @Override
                    public void onSucesso(LoginResponse resposta) {
                        if (resposta == null
                                || resposta.getUser() == null
                                || resposta.getUser().getId() == null
                                || resposta.getUser().getId().trim().isEmpty()) {
                            destino.onErro(
                                    "O Supabase não retornou o ID da conta criada."
                            );
                            return;
                        }

                        SupabaseUser user = resposta.getUser();

                        String emailAuth = user.getEmail() == null
                                ? email.trim()
                                : user.getEmail().trim();

                        CriarPerfilAdminRequest perfilRequest =
                                new CriarPerfilAdminRequest(
                                        user.getId(),
                                        nome.trim(),
                                        emailAuth,
                                        telefone.trim(),
                                        perfilEscolhido
                                );

                        // A requisição RPC usa o token do Admin que já
                        // está salvo no SessaoManager.
                        usuarioRepository.criarPerfilPeloAdmin(
                                perfilRequest,
                                new RepassadorCallback<Void, Boolean>(destino) {

                                    @Override
                                    public void onSucesso(Void ignorado) {
                                        destino.onSucesso(Boolean.TRUE);
                                    }

                                    @Override
                                    public void onErro(String mensagem) {
                                        destino.onErro(
                                                "A conta foi criada no Auth, mas o perfil "
                                                        + "não foi concluído. "
                                                        + mensagem
                                                        + " Verifique o banco antes de repetir."
                                        );
                                    }

                                    @Override
                                    public void onSessaoExpirada() {
                                        destino.onSessaoExpirada();
                                    }
                                }
                        );
                    }

                    @Override
                    public void onErro(String mensagem) {
                        destino.onErro(mensagem);
                    }

                    @Override
                    public void onSessaoExpirada() {
                        destino.onSessaoExpirada();
                    }
                }
        );

        return Resultado.ok();
    }

    // =====================================================
    // VALIDAÇÕES
    // =====================================================

    private Resultado validarCredenciais(
            String email,
            String senha
    ) {
        if (email == null || email.trim().isEmpty()) {
            return Resultado.erro(
                    Resultado.Campo.EMAIL,
                    "Informe o e-mail."
            );
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            return Resultado.erro(
                    Resultado.Campo.EMAIL,
                    "Informe um e-mail válido."
            );
        }

        if (senha == null || senha.trim().isEmpty()) {
            return Resultado.erro(
                    Resultado.Campo.SENHA,
                    "Informe a senha."
            );
        }

        return Resultado.ok();
    }

    private Resultado validarCadastro(
            String nome,
            String telefone,
            String email,
            String senha
    ) {
        if (nome == null || nome.trim().isEmpty()) {
            return Resultado.erro(
                    Resultado.Campo.NOME,
                    "Informe o nome completo."
            );
        }

        if (telefone == null || telefone.trim().isEmpty()) {
            return Resultado.erro(
                    Resultado.Campo.NENHUM,
                    "Informe o telefone."
            );
        }

        if (email == null || email.trim().isEmpty()) {
            return Resultado.erro(
                    Resultado.Campo.EMAIL,
                    "Informe o e-mail."
            );
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            return Resultado.erro(
                    Resultado.Campo.EMAIL,
                    "Informe um e-mail válido."
            );
        }

        if (senha == null || senha.isEmpty()) {
            return Resultado.erro(
                    Resultado.Campo.SENHA,
                    "Informe a senha."
            );
        }

        if (senha.length() < 6) {
            return Resultado.erro(
                    Resultado.Campo.SENHA,
                    "A senha deve ter pelo menos 6 caracteres."
            );
        }

        return Resultado.ok();
    }

    // =====================================================
    // AUXILIARES
    // =====================================================

    private boolean salvarSessaoSeHouver(LoginResponse resposta) {
        if (resposta == null
                || resposta.getAccessToken() == null
                || resposta.getAccessToken().trim().isEmpty()
                || resposta.getUser() == null
                || resposta.getUser().getId() == null
                || resposta.getUser().getId().trim().isEmpty()) {
            return false;
        }

        sessaoManager.salvarSessao(
                resposta.getAccessToken(),
                resposta.getRefreshToken(),
                resposta.getUser().getId()
        );

        return true;
    }

    private String normalizarPerfil(String perfil) {
        return perfil == null
                ? ""
                : perfil.trim().toUpperCase();
    }

    private boolean perfilValido(String perfil) {
        return "CLIENTE".equals(perfil)
                || "BARBEIRO".equals(perfil)
                || "ADMIN".equals(perfil);
    }
}