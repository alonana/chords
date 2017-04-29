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

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private HashMap<String, Clip> sounds;
    private HashMap<String, JCheckBox> checkboxes;
    private JFrame selectionFrame;

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
        new Game(checkboxes, this);
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

    HashMap<String, Clip> getSounds() {
        return sounds;
    }
}
