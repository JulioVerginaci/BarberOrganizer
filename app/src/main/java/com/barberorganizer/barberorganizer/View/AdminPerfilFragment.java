package com.barberorganizer.barberorganizer.View;

import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.barberorganizer.R;
import com.barberorganizer.barberorganizer.Core.Session.SessaoManager;

public class AdminPerfilFragment extends Fragment {
    @Nullable @Override public View onCreateView(@NonNull android.view.LayoutInflater inflater,@Nullable ViewGroup container,@Nullable Bundle state){
        android.content.Context c=requireContext(); ScrollView scroll=new ScrollView(c);scroll.setBackgroundColor(c.getColor(R.color.bg_dark));LinearLayout body=new LinearLayout(c);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(dp(22),dp(24),dp(22),dp(24));scroll.addView(body);
        TextView title=t("Perfil do Administrador",22,c.getColor(R.color.text_white),true);body.addView(title);
        SessaoManager sessao=new SessaoManager(c);String perfil=sessao.getPerfilUsuario();String id=sessao.getUsuarioId();
        LinearLayout card=new LinearLayout(c);card.setOrientation(LinearLayout.VERTICAL);card.setPadding(dp(16),dp(16),dp(16),dp(16));GradientDrawable d=new GradientDrawable();d.setColor(c.getColor(R.color.card_background));d.setCornerRadius(dp(16));card.setBackground(d);LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,-2);cp.topMargin=dp(18);body.addView(card,cp);
        card.addView(t("Tipo de acesso",12,c.getColor(R.color.text_gray),false));card.addView(t(perfil==null?"ADMINISTRADOR":perfil,16,c.getColor(R.color.gold_accent),true));TextView user=t("ID da conta: "+(id==null?"indisponível":id),12,c.getColor(R.color.text_white),false);user.setPadding(0,dp(12),0,0);card.addView(user);
        TextView sair=t("Sair da conta",14,c.getColor(R.color.bg_dark),true);sair.setGravity(Gravity.CENTER);sair.setPadding(dp(12),dp(13),dp(12),dp(13));GradientDrawable gd=new GradientDrawable();gd.setColor(c.getColor(R.color.gold_accent));gd.setCornerRadius(dp(14));sair.setBackground(gd);sair.setClickable(true);sair.setFocusable(true);LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,-2);sp.topMargin=dp(24);body.addView(sair,sp);
        sair.setOnClickListener(v->{new SessaoManager(requireContext()).limparSessao();Intent i=new Intent(requireContext(),MainActivity.class);i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);startActivity(i);requireActivity().finish();});return scroll;
    }
    private TextView t(String s,int size,int color,boolean bold){TextView v=new TextView(requireContext());v.setText(s);v.setTextSize(size);v.setTextColor(color);if(bold)v.setTypeface(null,Typeface.BOLD);return v;}
    private int dp(float n){return (int)(n*getResources().getDisplayMetrics().density+0.5f);}
}
