package com.chatsystem.user;

import javax.swing.*;
import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

/** Captures a live Swing viewer of actual git output, not a simulated GitHub page. */
public final class GitEvidence {
    private GitEvidence(){}
    public static void main(String[] args)throws Exception{
        Process p=new ProcessBuilder("git","log","--max-count=32","--format=%h  %an  %s","origin/main..HEAD").redirectErrorStream(true).start();
        String log=new String(p.getInputStream().readAllBytes(),StandardCharsets.UTF_8);if(p.waitFor()!=0)throw new IllegalStateException(log);
        SwingUtilities.invokeAndWait(()->{try{
            Ui.setup();JFrame f=new JFrame("Minh chứng Git local · Nhánh Phúc · Chưa push");f.setSize(1400,1000);f.setLocationRelativeTo(null);
            JTextArea text=new JTextArea(log);text.setEditable(false);text.setFont(new Font("Consolas",Font.PLAIN,14));text.setMargin(new Insets(16,16,16,16));f.add(new JScrollPane(text));f.setVisible(true);
            PreviewCapture.output=Path.of(args[0]);Files.createDirectories(PreviewCapture.output);
            PreviewCapture.save(f,"commits-phuc");f.dispose();
        }catch(Exception e){throw new RuntimeException(e);}});
    }
}
