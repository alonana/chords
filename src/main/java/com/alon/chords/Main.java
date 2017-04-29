package com.alon.chords;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private HashMap<String, Clip> sounds;
    private HashMap<String, JCheckBox> checkboxes;
    private JFrame selectionFrame;
    private LinkedList<String> selectedChords;
    private String lastPlayedChord;

    public static void main(String[] args) throws Exception {
        logger.info("starting");
        new Main();
        logger.info("done");
    }

    private Main() throws Exception {
        loadSounds();
        addGuiItems();
    }

    private void addGuiItems() {
        JPanel checkboxes = new JPanel(new FlowLayout());
        LinkedList<String> names = new LinkedList<>(sounds.keySet());
        this.checkboxes = new HashMap<>();
        for (String name : names) {
            JCheckBox checkbox = new JCheckBox(name, false);
            checkboxes.add(checkbox);
            this.checkboxes.put(name, checkbox);
        }

        JButton button = new JButton("start");
        button.addActionListener(this::start);

        selectionFrame = new JFrame();
        selectionFrame.getContentPane().setLayout(
                new BoxLayout(selectionFrame.getContentPane(), BoxLayout.PAGE_AXIS)
        );
        selectionFrame.setSize(400, 200);
        selectionFrame.add(checkboxes);
        selectionFrame.add(button);
        selectionFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        selectionFrame.setVisible(true);
    }

    private void start(ActionEvent actionEvent) {
        selectionFrame.setVisible(false);

        final JFrame gameFrame = new JFrame();
        gameFrame.setLayout(new FlowLayout());
        gameFrame.setSize(400, 200);
        selectedChords = new LinkedList<>();
        for (String name : checkboxes.keySet()) {
            JCheckBox checkBox = checkboxes.get(name);
            if (checkBox.isSelected()) {
                selectedChords.add(name);
            }
        }

        for (String name : selectedChords) {
            JButton button = new JButton(name);
            button.addActionListener(this::guess);
            button.setSize(100, 100);
            gameFrame.add(button);
        }

        gameFrame.setVisible(true);
        gameFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                gameFrame.setVisible(false);
                selectionFrame.setVisible(true);
                gameFrame.dispose();
            }
        });

        playRandom();
    }

    private void guess(ActionEvent actionEvent) {
        JButton button = (JButton) actionEvent.getSource();
        String guessed = button.getText();
        if (guessed.equals(lastPlayedChord)) {
            playRandom();
        } else {

        }
    }

    private void playRandom() {
        int playIndex = new Random().nextInt(selectedChords.size());
        lastPlayedChord = selectedChords.get(playIndex);
        Clip clip = sounds.get(lastPlayedChord);
        clip.setMicrosecondPosition(0);
        clip.start();
    }

    private void loadSounds() throws Exception {
        ClassLoader cl = this.getClass().getClassLoader();
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(cl);
        Resource[] resources = resolver.getResources("wav/*.wav");
        sounds = new HashMap<>();
        for (Resource resource : resources) {
            File file = resource.getFile();
            logger.info("loading wav file {}", file.getName());
            AudioInputStream stream = AudioSystem.getAudioInputStream(file);
            AudioFormat format = stream.getFormat();
            DataLine.Info info = new DataLine.Info(Clip.class, format);
            Clip clip = (Clip) AudioSystem.getLine(info);
            clip.open(stream);
            sounds.put(file.getName().replace(".wav", ""), clip);
        }
    }
}
