
package com.barberorganizer.barberorganizer.View;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.ColorInt;

import com.barberorganizer.R;
import com.barberorganizer.barberorganizer.Model.Agendamento;
import com.barberorganizer.barberorganizer.Repository.AgendamentoRepository;

public final class AgendaUi {
    private AgendaUi() { }

    public static LinearLayout column(Context c) {
        LinearLayout l = new LinearLayout(c); l.setOrientation(LinearLayout.VERTICAL); l.setPadding(dp(c, 20), dp(c, 18), dp(c, 20), dp(c, 24));
        return l;
    }
    public static ScrollView scroll(Context c, LinearLayout inner) {
        ScrollView s = new ScrollView(c); s.setFillViewport(true); s.setClipToPadding(false); s.addView(inner); return s;
    }
    public static TextView title(Context c, String t) {
        TextView v = text(c, t, 22, R.color.text_white, true); v.setPadding(0, dp(c, 2), 0, dp(c, 14)); return v;
    }
    public static TextView text(Context c, String t, int sp, int color, boolean bold) {
        TextView v = new TextView(c); v.setText(t == null ? "" : t); v.setTextSize(sp); v.setTextColor(resolveColor(c, color));
        if (bold) v.setTypeface(Typeface.DEFAULT, Typeface.BOLD); v.setGravity(Gravity.CENTER_VERTICAL); return v;
    }
    public static Button button(Context c, String label) {
        Button b = new Button(c); b.setText(label); b.setTextColor(c.getColor(R.color.bg_dark)); b.setAllCaps(false);
        b.setTextSize(14); b.setTypeface(Typeface.DEFAULT, Typeface.BOLD); b.setBackground(tint(c, c.getColor(R.color.gold_accent), 18));
        b.setPadding(dp(c, 12), dp(c, 5), dp(c, 12), dp(c, 5)); return b;
    }
    public static LinearLayout card(Context c) {
        LinearLayout l = new LinearLayout(c); l.setOrientation(LinearLayout.VERTICAL); l.setPadding(dp(c, 16), dp(c, 14), dp(c, 16), dp(c, 14));
        l.setBackground(tint(c, c.getColor(R.color.card_background), 18));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2); p.bottomMargin = dp(c, 10); l.setLayoutParams(p); return l;
    }
    public static void addGap(Context c, LinearLayout parent, int heightDp) {
        View v = new View(c); parent.addView(v, new LinearLayout.LayoutParams(1, dp(c, heightDp)));
    }
    public static void addBookingCard(Context c, LinearLayout parent, Agendamento a, boolean mostrarCliente, boolean permitirCancelar, Runnable cancelar) {
        LinearLayout card = card(c);
        String principal = mostrarCliente ? a.getNomeCliente() : a.getNomeBarbeiro();
        if (principal == null || principal.trim().isEmpty()) principal = mostrarCliente ? "Cliente" : "Barbeiro";
        card.addView(text(c, principal, 16, R.color.text_white, true));
        card.addView(text(c, (a.getServicos() == null || a.getServicos().isEmpty()) ? "Serviço não identificado" : a.getServicos(), 14, R.color.text_white, false));

        if (mostrarCliente) {
            String corte = a.getCorteFavoritoCliente();
            card.addView(text(c, "Corte favorito: " +
                            (corte == null ? "Carregando..." : corte),
                    13, R.color.gold_accent, true));
        }

        addGap(c, card, 5);
        card.addView(text(c, AgendamentoRepository.dataParaTela(a.getDataAgendamento()) + "  •  " + AgendamentoRepository.horaParaTela(a.getHorarioAgendamento()), 13, R.color.text_gray, false));
        addGap(c, card, 5);
        LinearLayout linha = new LinearLayout(c); linha.setGravity(Gravity.CENTER_VERTICAL); linha.setOrientation(LinearLayout.HORIZONTAL);
        TextView status = text(c, a.getNomeStatus(), 12, statusColor(c, a.getNomeStatus()), true); status.setPadding(dp(c, 8), dp(c, 4), dp(c, 8), dp(c, 4)); status.setBackground(tint(c, c.getColor(R.color.bg_dark), 10));
        linha.addView(status, new LinearLayout.LayoutParams(0, -2, 1));
        linha.addView(text(c, AgendamentoRepository.dinheiro(a.getValorCobrado()), 15, R.color.gold_accent, true));
        card.addView(linha);
        if (permitirCancelar) {
            addGap(c, card, 8);
            Button b = button(c, "Cancelar agendamento"); b.setOnClickListener(v -> cancelar.run()); card.addView(b);
        }
        parent.addView(card);
    }
    /** Aceita tanto R.color.* quanto uma cor ARGB já resolvida (por exemplo, 0xffffb300). */
    private static int resolveColor(Context c, int resourceOrColor) {
        if (((resourceOrColor >>> 24) & 0xff) == 0x7f) {
            return androidx.core.content.ContextCompat.getColor(c, resourceOrColor);
        }
        return resourceOrColor;
    }
    public static int dp(Context c, int value) { return Math.round(value * c.getResources().getDisplayMetrics().density); }
    private static GradientDrawable tint(Context c, @ColorInt int color, int radiusDp) {
        GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(c, radiusDp)); return d;
    }
    private static int statusColor(Context c, String status) {
        String n = status == null ? "" : status.toLowerCase(java.util.Locale.ROOT);
        if (n.contains("conclu")) return c.getColor(R.color.status_confirmed);
        if (n.contains("cancel")) return c.getColor(R.color.text_gray);
        if (n.contains("pend")) return c.getColor(R.color.status_pending);
        return c.getColor(R.color.status_confirmed);
    }
}
