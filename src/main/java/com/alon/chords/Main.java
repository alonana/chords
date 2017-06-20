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
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private HashMap<String, Clip> sounds;
    private HashMap<String, JCheckBox> checkboxes;
    private JFrame selectionFrame;
    private JTextField configRepeat;
    private JTextField configChords;
    private JButton startButton;

    public static void main(String[] args) throws Exception {
        logger.info("starting");
        new Main();
        logger.info("done");
    }

    private Main() throws Exception {
        loadSounds();
        addGuiItems();
        check(null);
    }

    private void addGuiItems() {
        JPanel checkboxes = new JPanel(new FlowLayout());
        LinkedList<String> names = new LinkedList<>();
        for (String name : sounds.keySet()) {
            name = name.substring(0, name.indexOf("_"));
            if (!names.contains(name)) {
                names.add(name);
            }
        }

        this.checkboxes = new HashMap<>();
        for (String name : names) {
            JCheckBox checkbox = new JCheckBox(name, false);
            checkbox.addActionListener(this::check);
            checkboxes.add(checkbox);
            this.checkboxes.put(name, checkbox);
        }

        configRepeat = new JTextField("1");
        configChords = new JTextField("2");

        startButton = new JButton("start");
        startButton.addActionListener(this::start);

        selectionFrame = new JFrame();
        selectionFrame.getContentPane().setLayout(
                new BoxLayout(selectionFrame.getContentPane(), BoxLayout.PAGE_AXIS)
        );
        selectionFrame.setSize(800, 200);
        selectionFrame.add(checkboxes);
        selectionFrame.add(new JLabel("Repeat:"));
        selectionFrame.add(configRepeat);
        selectionFrame.add(new JLabel("Chords:"));
        selectionFrame.add(configChords);
        selectionFrame.add(startButton);
        selectionFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        selectionFrame.setVisible(true);
    }

    private void check(ActionEvent actionEvent) {
        for (JCheckBox checkBox : checkboxes.values()) {
            if (checkBox.isSelected()) {
                startButton.setEnabled(true);
                return;
            }
        }
        startButton.setEnabled(false);
    }

    private void start(ActionEvent actionEvent) {
        selectionFrame.setVisible(false);
        try {
            new Game(checkboxes, this,
                    Integer.parseInt(configRepeat.getText()), Integer.parseInt(configChords.getText()));
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private void loadSounds() throws Exception {
        ClassLoader cl = this.getClass().getClassLoader();
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(cl);
        Resource[] resources = resolver.getResources("wav/*.wav");
        sounds = new HashMap<>();
        for (Resource resource : resources) {
            String name = resource.getFilename();
            logger.info("loading wav file {}", name);
            InputStream in = new BufferedInputStream(getClass().getClassLoader().getResourceAsStream("wav/" + name));
            AudioInputStream stream = AudioSystem.getAudioInputStream(in);
            AudioFormat format = stream.getFormat();
            DataLine.Info info = new DataLine.Info(Clip.class, format);
            Clip clip = (Clip) AudioSystem.getLine(info);
            clip.open(stream);
            sounds.put(name.replace(".wav", ""), clip);
        }
    }

    void back() {
        selectionFrame.setVisible(true);
    }

    Clip getRandomClip(String prefix) {
        LinkedList<Clip> relevant = new LinkedList<>();
        for (String name : sounds.keySet()) {
            if (name.startsWith(prefix + "_")) {
                relevant.add(sounds.get(name));
            }
        }

        int index = new Random().nextInt(relevant.size());
        logger.info("playing index " + index);
        return relevant.get(index);
    }
}
