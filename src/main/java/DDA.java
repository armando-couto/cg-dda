import java.applet.Applet;
import java.awt.Button;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class DDA extends Applet implements MouseListener, MouseMotionListener, ActionListener {
  int x1 = -1, y1 = -1, x2 = -1, y2 = -1,     //coordinates of start and end points
      pixelsize = 12,                     //size of pixel in raster
      xa = 0, xfin = 0, yfin = 0,         //increment and end point coordinates used in animation
      dif = 0;                            //increment on main axis
  Button refresh = new Button("   CLEAN   "); //button used to reset the drawing
  boolean drawLine = true, animate = false,   //vars to indicate the state of similarly named check boxes
      inProgress = false,                 //var used to indicate if the animation is in progress
      steep = false;                      //var used in animation
  volatile Thread vlakno;                     //thread used for animation
  double ya = 0, ma = 0;                      //vars used in animation

  public void setpix(int x, int y, int c) {   //draws a square on selected coordinates in raster
    Graphics g = getGraphics();
    g.setColor(new Color(c));
    g.fillRect(pixelsize * x, pixelsize * y, pixelsize, pixelsize);
  }

  public void rasterline(int xi, int yi, int xf, int yf) {
    //this method rasterizes the line if X shall be the main axis
    double y = yi;
    double m = (double) (yf - yi) / (xf - xi);   //calculates the direction
    if (Math.abs(m) <= 1) {                     //true if X is the main axis
      if (xi > xf) {                          //sets increment on the main (i.e. X) axis
        dif = -1;
      } else {
        dif = 1;
      }
      Graphics g = getGraphics();
      int strpos = 35;        //the following lines draws an info about the line and increments
      g.drawString("Real Coordinates", 10, 300 + strpos);
      strpos += 15;
      g.drawString("Start Point (real): [" + x1 + ", " + y1 + "]", 10, 300 + strpos);
      strpos += 15;
      g.drawString("End Point (real): [" + x2 + ", " + y2 + "]", 10, 300 + strpos);
      strpos += 25;
      g.drawString("Raster Coordinates", 10, 300 + strpos);
      strpos += 15;
      g.drawString("Start Point (raster): [" + xi + ", " + yi + "]", 10, 300 + strpos);
      strpos += 15;
      g.drawString("End Point (raster): [" + xf + ", " + yf + "]", 10, 300 + strpos);
      strpos += 25;
      g.drawString("Increment on X-axis = " + dif, 10, 300 + strpos);
      strpos += 15;
      if (xi > xf) {  //true if start point is to the right of the end point
        g.drawString("Increment on Y-axis = " + (double) Math.round(m * 100) / 100, 10, 300 + strpos);
        strpos += 15;
        g.setColor(new Color(0xFF0055));
        g.drawLine(645, 400, 645 + dif * 50, 400 - (int) (m * 50));
        g.fillOval(645 + dif * 50 - 5, 400 - (int) (m * 50) - 5, 10, 10);
        g.setColor(new Color(0x000000));
        for (int i = xi; i >= xf; i += dif) {             //the line rasterization itself
          setpix(i, (int) Math.round(y), 0x000000);
          y -= m;
        }
      } else {    //if the start point is to the left of the end point
        //works the same way as the statement before, only increment on y axis is inverted
        //(and the for condition is adjusted to work properly)
        g.drawString("Increment on Y-axis = " + (double) Math.round(-m * 100) / 100, 10, 300 + strpos);
        strpos += 15;
        g.setColor(new Color(0xFF0055));
        g.drawLine(645, 400, 645 + dif * 50, 400 + (int) (m * 50));
        g.fillOval(645 + dif * 50 - 5, 400 + (int) (m * 50) - 5, 10, 10);
        g.setColor(new Color(0x000000));
        for (int i = xi; i <= xf; i += dif) {
          setpix(i, (int) Math.round(y), 0x000000);
          y += m;
        }
      }
    } else {
      //if Y shall be the main axis, the call thes metho instead
      rastersteepline(yi, xi, yf, xf);
    }
  }

  public void rastersteepline(int xi, int yi, int xf, int yf) {
    //this method rasterizes the line if Y shall be the main axis
    //works similarly as the rasterline method, just swaps the main axis
    if (xi > xf) {  //sets the increment on main (i.e. Y) axis
      dif = -1;
    } else {
      dif = 1;
    }
    double y = yi;
    double m = (double) (yf - yi) / (xf - xi);   //calculates the direction
    Graphics g = getGraphics();
    int strpos = 35;        //the following lines draws an info about the line and increments
    g.drawString("Real Coordinates", 10, 300 + strpos);
    strpos += 15;
    g.drawString("Start Point: [" + x1 + ", " + y1 + "]", 10, 300 + strpos);
    strpos += 15;
    g.drawString("End Point: [" + x2 + ", " + y2 + "]", 10, 300 + strpos);
    strpos += 25;
    g.drawString("Raster Coordinates", 10, 300 + strpos);
    strpos += 15;
    g.drawString("Start Point: [" + xi + ", " + yi + "]", 10, 300 + strpos);
    strpos += 15;
    g.drawString("End Point: [" + xf + ", " + yf + "]", 10, 300 + strpos);
    strpos += 25;
    if (xi > xf) {  //true if start point is to the right of the end point
      g.drawString("Increment on X-axis = " + (double) Math.round(-m * 100) / 100, 10, 300 + strpos);
      strpos += 15;
      g.setColor(new Color(0xFF0055));
      g.drawLine(645, 400, 645 - (int) (m * 50), 400 + dif * 50);
      g.fillOval(645 - (int) (m * 50) - 5, 400 + dif * 50 - 5, 10, 10);
      g.setColor(new Color(0x000000));
      for (int i = xi; i >= xf; i += dif) {
        setpix((int) Math.round(y), i, 0x000000);
        y -= m;
      }
    } else {        //if start point is to the left of the end point
      g.drawString("Increment on X-axis = " + (double) Math.round(m * 100) / 100, 10, 300 + strpos);
      strpos += 15;
      g.setColor(new Color(0xFF0055));
      g.drawLine(645, 400, 645 + (int) (m * 50), 400 + dif * 50);
      g.fillOval(645 + (int) (m * 50) - 5, 400 + dif * 50 - 5, 10, 10);
      g.setColor(new Color(0x000000));
      for (int i = xi; i <= xf; i += dif) {
        setpix((int) Math.round(y), i, 0x000000);
        y += m;
      }
    }
    g.drawString("Increment on Y-axis = " + -dif, 10, 300 + strpos);
    strpos += 15;
  }

  public void init() {
    //sets up the environment
    setSize(800, 500);
    this.addMouseListener(this);
    this.addMouseMotionListener(this);

    refresh.addActionListener(this);
    this.add(refresh);
  }

  public void stop() {
    vlakno = null;
  }

  public void update(Graphics g) {
    //overrides the default update method that is called after aplication of repaint() command
    //this disables the pre-cleaning of the drawing area before the paint method is applied
    //that causes the flicker
    paint(g);
  }

  public void paint(Graphics g) {
    Dimension d = getSize();
    Color c1 = new Color(0x000000);
    Color c2 = new Color(0xCCCCCC);
    Color c3 = new Color(0xF5C24E);

    refresh.setLocation(380, 353);
    g.drawString("Raster Size:", 225, 370);

    //draws the mini coordinate system in right-bottom corner
    if (!inProgress) {
      g.drawLine(570, 400, 720, 400);
      g.drawLine(645, 325, 645, 475);
      g.drawLine(570, 325, 720, 475);
      g.drawLine(570, 475, 720, 325);
    }

    if ((x1 >= 0) && (y1 >= 0) && (x2 >= 0) && (y2 >= 0)) {
      //true if both the start and end point were selected
      if (inProgress) {    //true if the animation is in progress
        if (steep) {     //true if Y is the main axis
          setpix((int) Math.round(ya), xa, 0xA6EBFB);
        } else {
          setpix(xa, (int) Math.round(ya), 0xA6EBFB);
        }
      } else {    //if the animation is not in progress
        rasterline(x1 / pixelsize, y1 / pixelsize, x2 / pixelsize, y2 / pixelsize);
      }

      if (drawLine) {  //draws the "real" line, if drawLineCHX checkbox is checked
        g.setColor(c3);
        g.drawLine(x1, y1, x2, y2);
      }
    }

    g.clearRect(1, 301, d.width - 1, 20);

    //these following lines draws the raster lines
    g.setColor(c2);
    for (int i = pixelsize; i < d.width; i += pixelsize) {
      g.drawLine(i, 0, i, d.height - 200);
    }
    for (int i = pixelsize; i < d.height - 200; i += pixelsize) {
      g.drawLine(0, i, d.width, i);
    }

    g.setColor(c1);
    //draws the text in the mini coordinate system in right-bottom corner
    g.drawString("dx=1/m", 595, 320);
    g.drawString("dy=1", 595, 330);
    g.drawString("dx=1/m", 665, 320);
    g.drawString("dy=1", 665, 330);
    g.drawString("dx=1", 705, 370);
    g.drawString("dy=m", 705, 380);
    g.drawString("dx=1", 705, 430);
    g.drawString("dy=-m", 705, 440);
    g.drawString("dx=-1/m", 665, 475);
    g.drawString("dy=-1", 665, 485);
    g.drawString("dx=-1/m", 595, 475);
    g.drawString("dy=-1", 595, 485);
    g.drawString("dx=-1", 555, 430);
    g.drawString("dy=-m", 555, 440);
    g.drawString("dx=-1", 555, 370);
    g.drawString("dy=m", 555, 380);

    //these following lines draws the border lines
    g.drawLine(0, 0, d.width, 0);
    g.drawLine(0, 0, 0, d.height);
    g.drawLine(d.width - 1, d.height - 1, d.width - 1, 0);
    g.drawLine(d.width - 1, d.height - 200, 0, d.height - 200);
    g.drawLine(d.width - 1, d.height - 1, 0, d.height - 1);
  }

  public void actionPerformed(ActionEvent e) {
    //a method for the refresh ("CLEAN") buttton
    vlakno = null;      //turn of the animation process
    inProgress = false;
    x1 = -1;            //reset the line coordinates
    y1 = -1;
    x2 = -1;
    y2 = -1;
    Graphics g = getGraphics();
    g.clearRect(0, 0, getSize().width, getSize().height);    //clean the drawing area
    repaint();
  }

  public void mousePressed(MouseEvent e) {
    if (e.getY() < 300) {
      //disallows drawing of the line in the lower area, outside the raster
      if ((x1 < 0) || (y1 < 0)) { //if the start point is not set
        x1 = e.getX();
        y1 = e.getY();
        setpix(x1 / pixelsize, y1 / pixelsize, 0x00FF00);
      } else if (((x2 < 0) || (y2 < 0)) && (e.getX() != x1) && (e.getY() != y1)) {
        //if the end point is not set
        x2 = e.getX();
        y2 = e.getY();
        repaint();
      }
    }
  }

  public void mouseDragged(MouseEvent e) {
  }

  public void mouseReleased(MouseEvent e) {
  }

  public void mouseClicked(MouseEvent e) {
  }

  public void mouseEntered(MouseEvent e) {
  }

  public void mouseExited(MouseEvent e) {
  }

  public void mouseMoved(MouseEvent e) {
  }
}