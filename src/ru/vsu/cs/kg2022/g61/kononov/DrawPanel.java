package ru.vsu.cs.kg2022.g61.kononov;

import ru.vsu.cs.kg2022.g61.kononov.drawers.*;

//import javax.sound.sampled.Line;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

public class DrawPanel extends JPanel {
    private final ScreenConverter converter;
    private Line current = null;
    private Point lastP;
    private final java.util.List<Line> lines = new ArrayList<>();
    private static final java.util.List<Star> starList = new ArrayList<>();
    private static final double timeLength = 1;
    private double SIZE = 15;
    private boolean isInit = true;
    private final Random random = new Random();
    GridDrawing drawing;


    public DrawPanel() {

        converter = new ScreenConverter(800,600, -SIZE, SIZE, 2 * SIZE, 2 * SIZE);
        drawing = new GridDrawing(converter);

        this.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if(lastP != null){
                    Point curP = e.getPoint();
                    ScreenPoint delta = new ScreenPoint(-curP.x + lastP.x,-curP.y + lastP.y);
                    RealPoint deltaR = converter.s2r(delta);
                    converter.setX(deltaR.getX());
                    converter.setY(deltaR.getY());
                    lastP = curP;
                    converter.moveCorner(deltaR);
                }
                repaint();
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                /*ScreenPoint sp = new ScreenPoint(e.getX(), e.getY());
                current.setP2(converter.s2r(sp));
                repaint();*/
            }
        });

        this.addMouseWheelListener(e -> {
            int count = e.getWheelRotation();
            double base = count < 0 ? 0.99 : 1.01;
            double coef = 1;
            for (int i = Math.abs(count); i > 0 ; i--) {
                coef *= base;
            }
            converter.changeScale(coef);
            repaint();
        });

        this.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    lastP = e.getPoint();
                }
                if(SwingUtilities.isLeftMouseButton(e)){

                    ScreenPoint p = new ScreenPoint(e.getX(), e.getY());
                    current = new Line(converter.s2r(p), converter.s2r(p));
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    lastP = null;
                }
                if(SwingUtilities.isLeftMouseButton(e)){
                    lines.add(current);
                    current = null;
                    repaint();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        converter.setsHeight(getHeight());
        converter.setsWidth(getWidth());

        BufferedImage bi = new BufferedImage(getWidth(),getHeight(),BufferedImage.TYPE_INT_RGB);
        Graphics2D biG = bi.createGraphics();
        biG.setColor(Color.WHITE);
        biG.fillRect(0,0,getWidth(),getHeight());

        //LineDrawer ld = new DDALineDrawer(new GraphicsPixelDrawer(biG));
        LineDrawer ld = new BresenhamLineDrawer(new GraphicsPixelDrawer(biG));
        //LineDrawer ld = new WULineDrawer(new GraphicsPixelDrawer(biG));

        biG.setColor(Color.BLACK);


        drawing.draw(ld,biG);


        Star currStar;

        if (isInit) {
            for (int i = 0; i < 10; i++) {
                int x = (int) (Math.random() * 50);
                int y = (int) (Math.random() * 50);
                currStar = new Star(new RealPoint(x, y), 1, 40, 20);
                starList.add(currStar);
            }

            isInit = false;
        }







        for (int i = 0; i < starList.size() - 1; i++){

            if (i == 0) {
                drawStar(ld, converter, starList.get(i));
            } else {
                drawStar(ld, converter, starList.get(i+1));
            }
        }


        g2d.drawImage(bi,0,0,null);
        biG.dispose();
    }

    private void drawStar(LineDrawer drawer, ScreenConverter converter, Star star){
        ScreenPoint center = converter.r2s(star.getCenter());

        int coreR = star.getCoreR();
        int rayR = star.getRayR();
        int n = star.getRays();

        double da = 2 * Math.PI / n;
        for (int i = 0; i < n; i++){
            double a = da * i;
            drawer.drawLine(
                    (int) (center.getX() + coreR * Math.cos(a)),
                    (int) (center.getY() + coreR * Math.sin(a)),
                    (int) (center.getX() + rayR * Math.cos(a)),
                    (int) (center.getY() + rayR * Math.sin(a)));
        }
    }
}
