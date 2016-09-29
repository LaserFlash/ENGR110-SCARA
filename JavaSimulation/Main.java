//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

//package ToWebSite;

//import ToWebSite.Arm;
//import ToWebSite.Drawing;
//import ToWebSite.PointXY;
//import ToWebSite.ToolPath;
import ecs100.UI;
import ecs100.UIButtonListener;
import ecs100.UIFileChooser;
import ecs100.UIKeyListener;
import ecs100.UIMouseListener;
import java.awt.Color;

import java.awt.geom.Arc2D;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

public class Main {
    private Arm arm;
    private Drawing drawing;
    private ToolPath tool_path;
    private int state;

    public Main() {
        UI.initialise();
        UI.addButton("Clear Drawing", ()-> drawing = new Drawing());
        UI.addButton("xy to angles", this::inverse);
        UI.addButton("Enter path XY", this::enter_path_xy);
        UI.addButton("Save path XY", this::save_xy);
        UI.addButton("Load path XY", this::load_xy);
        UI.addButton("Save path Ang", this::save_ang);
        UI.addButton("Load path Ang:Play", this::load_ang);
        UI.addButton("Check directkinematics", this::checkDirect);
        UI.addButton("Save Pulse", this::savePulse);
        UI.addButton("Send pulses to RPi",this::sendPulse);
        UI.addButton("Circle", this::drawCircle);
        UI.addButton("Line", this::drawLine);
        UI.addButton("Square", this::drawSquare);
        UI.addButton("Elf", this::interpretSVG);

        UI.setMouseMotionListener(this::doMouse);
        UI.setKeyListener(this::doKeys);
        this.arm = new Arm();
        this.drawing = new Drawing();
        tool_path = new ToolPath();
        this.arm.draw();
    }

    public void checkDirect(){
        state = 4;
    }

    public void savePulse(){
        this.tool_path.save_pwm_file(drawing, arm);
    }

    public void sendPulse(){
        try {
            Runtime.getRuntime().exec("expect scp.exp");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void doKeys(String action) {
        UI.printf("Key :%s \n", action);
        if(action.equals("b")) {
            this.state = 3;
        }

    }

    public void doMouse(String action, double x, double y) {
        if(x >= 640 || y >=480) return;
        UI.clearGraphics();
        String out_str = String.format("%3.1f %3.1f", Double.valueOf(x), Double.valueOf(y));
        UI.drawString(out_str, x + 10.0D, y + 10.0D);
        this.arm.drawField();
        this.drawing.draw();
        if(this.state == 1 && action.equals("clicked")) {
            this.arm.inverseKinematic(x, y);
            this.arm.draw();
        } else {
            if((this.state == 2 || this.state == 3) && action.equals("moved")) {
                this.arm.inverseKinematic(x, y);
                this.arm.draw();
                if(this.state == 2 && this.drawing.get_path_size() > 0) {
                    new PointXY();
                    PointXY lp = this.drawing.get_path_last_point();
                    UI.setColor(Color.GRAY);
                    UI.drawLine(lp.get_x(), lp.get_y(), x, y);
                }

                this.drawing.draw();
            }

            if(this.state == 2 && action.equals("clicked")) {
                UI.printf("Adding point x=%f y=%f\n", Double.valueOf(x), Double.valueOf(y));
                this.drawing.add_point_to_path(x, y, true);
                this.arm.inverseKinematic(x, y);
                this.arm.draw();
                this.drawing.draw();
                this.drawing.print_path();
            }

            if(this.state == 3 && action.equals("clicked")) {
                this.drawing.add_point_to_path(x, y, false);
                this.arm.inverseKinematic(x, y);
                this.arm.draw();
                this.drawing.draw();
                this.drawing.print_path();
                this.state = 2;
            }
            if(this.state == 4 && action.equals("clicked")){
                this.arm.inverseKinematic(x,y);
                this.arm.draw();
                this.arm.directKinematic();

            }

        }
    }

    public void save_xy() {
        this.state = 0;
        String fname = UIFileChooser.save();
        this.drawing.save_path(fname);
    }

    public void enter_path_xy() {
        this.state = 2;
    }

    public void inverse() {
        this.state = 1;
        this.arm.draw();
    }

    public void load_xy() {
        this.state = 0;
        String fname = UIFileChooser.open();
        this.drawing.load_path(fname);
        this.drawing.draw();
    }

    public void save_ang() {
        String fname = UIFileChooser.save();
        this.tool_path.convert_drawing_to_angles(this.drawing, this.arm, fname);
    }

    public void load_ang() {
        try {
            Scanner sc = new Scanner(new File(UIFileChooser.open()));
            while (sc.hasNext()) {
                double x = Double.parseDouble(sc.nextLine());
                double y = Double.parseDouble(sc.nextLine());
                arm.set_angles(x * Math.PI/180,y* Math.PI/180);
                arm.directKinematic();
                if (Double.parseDouble(sc.nextLine()) == 1) {
                    this.drawing.add_point_to_path(arm.getxTool(), arm.getyTool(), true);
                } else this.drawing.add_point_to_path(arm.getxTool(), arm.getyTool(), false);
                drawing.draw();
            }
        }catch (IOException e){UI.println(e);}
    }

    public void drawCircle(){
        circle(150, 200, 200);
    }

    public void circle(int r, int xMid, int yMid){
        drawing = new Drawing();
        double incr = 0.1;
        for  (double i = 0; i - incr <= 2* Math.PI; i += incr){
            drawing.add_point_to_path(xMid + (r) * Math.sin(i), yMid + (r) * Math.cos(i), true);
        }
        drawing.draw();
    }

    public void drawLine(){
        drawing = new Drawing();
        int xOffset = 200;
        int yOffset = 200;
        int increments = 10;
        int l = 100;
        for (int i = 0; i < l; i+= increments){
            drawing.add_point_to_path(i+ xOffset, yOffset, true );
            drawing.draw();
        }
    }

    public void drawSquare(){
        drawing = new Drawing();
        int xOffset = 200;
        int yOffset = 200;
        int increments = 10;
        int l = 100;
        for (int i = 0; i < l; i+= increments){
            drawing.add_point_to_path(i+ xOffset, yOffset, true );
            drawing.draw();
        }
        for (int i = 0; i < l; i+= increments){
            drawing.add_point_to_path(l + xOffset, yOffset + i, true );
            drawing.draw();
        }
        for (int i = 0; i < l; i+= increments){
            drawing.add_point_to_path(xOffset + l - i, l + yOffset, true );
            drawing.draw();
        }
        for (int i = 0; i <= l; i+= increments){
            drawing.add_point_to_path(xOffset, yOffset + l - i, true );
            drawing.draw();
        }
    }
    //woodRock made original SVG interpreter, unlike most others
    public void interpretSVG(){
        String fileName = UI.askString("Filename:");
        drawing = new Drawing();
        try {
            Scanner sc_for_spaces = new Scanner(new File(fileName + ".txt"));
            PrintStream out = new PrintStream(new File(fileName + "2.txt"));
            while(sc_for_spaces.hasNext()){
                out.print(" ");
                out.print(addSpaces(sc_for_spaces.next()));
            }
            
            Scanner sc = new Scanner(new File(fileName + "2.txt"));
            while(sc.hasNext()) {
                String string = sc.next();
                //System.out.println("here");
                double x = 200;
                double y = 75;
                double scalar = 0.60;
                if (string.equals("L")) {
                    x += scalar * sc.nextDouble();
                    y += scalar * sc.nextDouble();
                    drawing.add_point_to_path(x, y, true);
                    UI.printf("x:%.2f, y:%.2f, true \n", x, y);
                }
                if (string.equals("M")) {
                    x += scalar * sc.nextDouble();
                    y += scalar * sc.nextDouble();
                    UI.println("here");
                    drawing.add_point_to_path(x, y, false);
                    UI.printf("x:%.2f, y:%.2f, false \n", x, y);
                }
            }
            drawing.draw();
        }
        catch (Exception e) { System.out.println("Java Exception" + e);}
    }

    public String addSpaces(String token){
        ArrayList<String> temp = new ArrayList<String>();
        String result = "";
        for (int i = 0; i < token.length() - 1; i++){
            String character = token.substring(i, i+1);
            if (character.equals("L") || character.equals("M")){
                character = " " + character + " ";
            }
            if (character == " "){
                character = " ";
            }
            temp.add(character);
        }
        for (String s: temp){
            result += s;
        }
        UI.println(result);
        return result;
    }


    public void skyNet(){}

    public void snowMan(){}


    public static void main(String[] args) {
        new Main();
    }
}
