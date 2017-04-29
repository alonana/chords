package com.alon.chords;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

class Game {
    private final Main main;
    private LinkedList<String> selectedChords;
    private String lastPlayedChord;
    private JLabel pointsLabel;
    private LinkedList<JButton> buttons;

    Game(HashMap<String, JCheckBox> checkboxes, Main main) {
        this.main = main;
        selectedChords = new LinkedList<>();
        for (String name : checkboxes.keySet()) {
            JCheckBox checkBox = checkboxes.get(name);
            if (checkBox.isSelected()) {
                selectedChords.add(name);
            }
        }

        buildGui(main);
        playRandom();
    }

    private void buildGui(final Main main) {
        JPanel buttonsPanel = buildGuiButtons();

        JPanel points = new JPanel(new FlowLayout());
        pointsLabel = new JLabel("0");
        pointsLabel.setSize(300, 300);
        pointsLabel.setFont(new Font("Arial", Font.PLAIN, 40));
        points.add(pointsLabel);

        buildGuiRoot(main, buttonsPanel, points);
    }

    private void buildGuiRoot(final Main main, JPanel buttonsPanel, JPanel points) {
        final JFrame gameFrame = new JFrame();
        gameFrame.getContentPane().setLayout(new GridLayout(2, 1));
        gameFrame.add(buttonsPanel);
        gameFrame.add(points);
        gameFrame.setSize(1000, 500);
        gameFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                gameFrame.setVisible(false);
                gameFrame.dispose();
                main.back();
            }
        });
        resetButtons();
        gameFrame.setVisible(true);
    }

    private JPanel buildGuiButtons() {
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new GridLayout(1, selectedChords.size()));

        buttons = new LinkedList<>();
        for (String name : selectedChords) {
            JButton button = new JButton(name);
            buttons.add(button);
            button.setSize(300, 300);
            button.addActionListener(this::guess);
            buttonsPanel.add(button);
        }
        return buttonsPanel;
    }

    private void guess(ActionEvent actionEvent) {
        try {
            guessWrapped(actionEvent);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void guessWrapped(ActionEvent actionEvent) throws Exception {
        JButton button = (JButton) actionEvent.getSource();
        String guessed = button.getText();
        int points = Integer.parseInt(pointsLabel.getText());
        if (guessed.equals(lastPlayedChord)) {
            playEffect("ok");
            Thread.sleep(500);
            points++;
            resetButtons();
            playRandom();
        } else {
            playEffect("wrong");
            button.setEnabled(false);
            button.setBackground(Color.RED);
            button.setForeground(Color.BLACK);
            points--;
            if (points < 0) {
                points = 0;
            }
        }
        pointsLabel.setText(Integer.toString(points));
    }

    private void resetButtons() {
        for (JButton button : buttons) {
            button.setEnabled(true);
            button.setBackground(Color.BLUE);
            button.setForeground(Color.YELLOW);
            button.setFont(new Font("Arial", Font.PLAIN, 40));
        }
    }

    private void playRandom() {
        int playIndex = new Random().nextInt(selectedChords.size());
        lastPlayedChord = selectedChords.get(playIndex);
        Clip clip = main.getSounds().get(lastPlayedChord);
        clip.setMicrosecondPosition(0);
        clip.start();
    }

    private void playEffect(String name) throws Exception {
        InputStream in = new BufferedInputStream(getClass().getClassLoader().getResourceAsStream("effect/" + name + ".wav"));
        AudioInputStream stream = AudioSystem.getAudioInputStream(in);
        AudioFormat format = stream.getFormat();
        DataLine.Info info = new DataLine.Info(Clip.class, format);
        Clip clip = (Clip) AudioSystem.getLine(info);
        clip.open(stream);
        clip.start();
    }
}
