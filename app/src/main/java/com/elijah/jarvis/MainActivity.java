package com.elijah.jarvis;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {
    private TextView status;
    private TextToSpeech voz;
    private static final int FALA = 10;

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);

        LinearLayout tela = new LinearLayout(this);
        tela.setOrientation(1);
        tela.setGravity(Gravity.CENTER);
        tela.setPadding(35, 35, 35, 35);
        tela.setBackgroundColor(Color.rgb(8, 17, 30));

        TextView titulo = new TextView(this);
        titulo.setText("J.A.R.V.I.S.");
        titulo.setTextSize(34);
        titulo.setTextColor(Color.CYAN);
        titulo.setGravity(Gravity.CENTER);
        tela.addView(titulo);

        status = new TextView(this);
        status.setText("Sistema pronto. Toque para falar.");
        status.setTextColor(Color.WHITE);
        status.setTextSize(18);
        status.setGravity(Gravity.CENTER);
        status.setPadding(0, 40, 0, 40);
        tela.addView(status);

        Button botao = new Button(this);
        botao.setText("FALAR COM JARVIS");
        tela.addView(botao);
        setContentView(tela);

        voz = new TextToSpeech(this, resultado -> {
            if (resultado == TextToSpeech.SUCCESS) {
                voz.setLanguage(new Locale("pt", "BR"));
            }
        });

        botao.setOnClickListener(v -> ouvir());
    }

    private void ouvir() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                new String[]{Manifest.permission.RECORD_AUDIO}, 2);
            return;
        }

        Intent i = new Intent(
            RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-BR");

        try {
            startActivityForResult(i, FALA);
        } catch (Exception e) {
            status.setText("Reconhecimento de voz indisponível.");
        }
    }

    @Override
    protected void onActivityResult(
        int request, int result, Intent data) {
        super.onActivityResult(request, result, data);

        if (request != FALA || result != RESULT_OK
                || data == null) return;

        ArrayList<String> frases =
            data.getStringArrayListExtra(
                RecognizerIntent.EXTRA_RESULTS);

        if (frases == null || frases.isEmpty()) return;

        String comando = frases.get(0).toLowerCase(
            new Locale("pt", "BR"));
        String resposta;

        if (comando.contains("hora")) {
            resposta = "Agora são " +
                new java.text.SimpleDateFormat(
                    "HH:mm", new Locale("pt", "BR"))
                    .format(new java.util.Date());
        } else if (comando.contains("seu nome")
                || comando.contains("quem é você")) {
            resposta = "Sou JARVIS, seu assistente pessoal.";
        } else if (comando.contains("olá")
                || comando.contains("oi jarvis")) {
            resposta = "Olá! Estou à sua disposição.";
        } else {
            resposta = "Entendi: " + frases.get(0)
                + ". Ainda estou aprendendo novos comandos.";
        }

        status.setText("Você: " + frases.get(0)
            + "\n\nJARVIS: " + resposta);

        if (voz != null) {
            voz.speak(resposta, TextToSpeech.QUEUE_FLUSH,
                null, "resposta");
        }
    }

    @Override
    protected void onDestroy() {
        if (voz != null) {
            voz.stop();
            voz.shutdown();
        }
        super.onDestroy();
    }
}
