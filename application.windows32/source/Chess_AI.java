import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import processing.sound.SoundFile; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class Chess_AI extends PApplet {



final int w= 100;
PImage boardImage;
final char[] sequence= {'p', 'n', 'b', 'r', 'q', 'k', 'P', 'N', 'B', 'R', 'Q', 'K'};
HashMap<Character, PImage> piecesImages;
static char[][] board;
final int YOU= 1;
final int AI= -YOU;
final int AILevel= 2;
static int kingPosB, kingPosW;
int selected= -1;
StringList choices= new StringList();
int gameWon= 0;
int selectColor, choiceColor, dangerColor, promotionColor;
SoundFile moveSound, captureSound, checkSound, promotionSound;
public void settings() {
  size(w*8, w*8);
}

public void setup() {
  boardImage= loadImage("board.png");
  boardImage.resize(width, height);

  board= new char[][]{
    {'r', 'n', 'b', 'q', 'k', 'b', 'n', 'r'}, 
    {'p', 'p', 'p', 'p', 'p', 'p', 'p', 'p'}, 
    {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '}, 
    {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '}, 
    {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '}, 
    {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '}, 
    {'P', 'P', 'P', 'P', 'P', 'P', 'P', 'P'}, 
    {'R', 'N', 'B', 'Q', 'K', 'B', 'N', 'R'}
  };
  for (int i=0; i<8; i++) {
    for (int j=0; j<8; j++) {
      //if (board[i][j]=='P') board[i][j]= ' ';
      //if(board[i][j]=='R') board[i][j]= ' ';
      //if (board[i][j]=='p') board[i][j]= ' ';
    }
  }
  piecesImages= new HashMap<Character, PImage>();
  for (int i=0; i<=1; i++) {
    for (int j=0; j<6; j++) {
      piecesImages.put(sequence[i*6+j], loadImage("pieces/"+i+j+".png"));
      piecesImages.get(sequence[i*6+j]).resize(w, w);
    }
  }
  while (pieceAt(kingPosB) != 'k') {
    kingPosB++;
  }
  while (pieceAt(kingPosW) != 'K') {
    kingPosW++;
  }
  noStroke();
  PFont roboto= createFont("Roboto-Medium.ttf", 20);
  textFont(roboto);
  textAlign(CENTER, CENTER);

  selectColor= color(0xff0092FF, 120);
  choiceColor= color(0xffFFE600, 120);
  dangerColor= color(0xffFC1831, 150);
  promotionColor= color(0xffF10AFF, 120);

  moveSound= new SoundFile(this, "sfx/move.mp3");
  captureSound= new SoundFile(this, "sfx/capture.mp3");
  checkSound= new SoundFile(this, "sfx/check.mp3");
  promotionSound= new SoundFile(this, "sfx/promotion.mp3");
}
int t= 0;
public void draw() {
  background(boardImage);
  if (selected!=-1) {
    drawRect(selectColor, selected);
  }

  if (!isKingSafe()) {
    drawRect(dangerColor, kingPosW);
  }
  flipBoard();
  if (!isKingSafe()) {
    drawRect(dangerColor, 63 - kingPosW);
  }
  flipBoard();
  
  if (runAI) {
    t++;
    if (t>60/frameRate) {
      runAIMove();
      runAI= false;
      t=0;
    }
  }

  for (String c : choices) {
    int i= index(getIntFromString(c, 3), getIntFromString(c, 2));
    if (c.charAt(4)=='$') {
      i= index(getIntFromString(c, 1), 0);
      drawRect(promotionColor, i);
    } else if (c.charAt(4)==' ') drawRect(choiceColor, i);
    else drawRect(dangerColor, i);
  }

  //if (possibleMoves().length()==0) gameWon= -turn;
  showPieces();

  if (gameWon!=0) {
    textSize(w*2);
    fill(70, 200);
    rect(0, 0, width, height);
    fill(gameWon==1 ? 255 : 0);
    text(gameWon==1 ? "White" : "Black", w*4, w*3.2f);
    textSize(w*1.1f);
    text("Wins", w*4, w*4.7f);
  }
}
int count=0;
public void runAIMove() {
  count= 0;
  long t= millis();

  flipBoard();
  String move= bestMove();
  if (possibleMoves().length()==0) {
    noLoop();
    println("YOU win");
    return;
  }
  makeMove(move);
  flipBoard();
  println(millis() - t +" milliseconds");
  println(count);
  if (!isKingSafe()) checkSound.play();
  else if (move.charAt(4)==' ') moveSound.play();
  else if (move.charAt(4)=='$') promotionSound.play();
  else captureSound.play();

  if (possibleMoves().length()==0) {
    noLoop();
    println("AI wins");
    return;
  }
}
public String randomMove() {
  String possibleMoves= possibleMoves();
  int i= PApplet.parseInt(random(possibleMoves.length()/5))*5;
  return possibleMoves.substring(i, i+5);
}

public String goodMove() {
  String possibleMoves= possibleMoves();
  String goodMove= possibleMoves.substring(0, 5);
  int bestScore= +99999;
  for (int i=0; i<possibleMoves.length(); i+=5) {
    String move= possibleMoves.substring(i, i+5);
    makeMove(move);
    int newScore= Rating.rating(possibleMoves.length(), 0);
    if (newScore < bestScore) {
      goodMove= move;
      bestScore= newScore;
    }
    undoMove(move);
  }
  return goodMove;
}

public String bestMove() {
  String possibleMoves= possibleMoves();
  int bestScore= +9999999;

  String bestMove= "";
  for (int i=0; i<possibleMoves.length(); i+=5) {
    String move= possibleMoves.substring(i, i+5);
    makeMove(move);
    flipBoard();
    int newScore= minimax(AILevel, YOU);
    flipBoard();
    undoMove(move);
    if (newScore < bestScore) {
      bestMove= move;
      bestScore= newScore;
    }
  }
  return bestMove;
}
public int minimax(int depth, int player) {
  String possibleMoves= possibleMoves();
  if (depth==0 || possibleMoves.length()==0) {
    return Rating.rating(possibleMoves.length(), depth);
  }
  int bestScore= 0;
  if (player==YOU) {
    bestScore= -9999999;
    for (int i=0; i<possibleMoves.length(); i+=5) {
      String move= possibleMoves.substring(i, i+5);
      makeMove(move);
      flipBoard();
      bestScore= max(bestScore, minimax(depth-1, AI));
      flipBoard();
      undoMove(move);
    }
  } else {
    bestScore= +9999999;
    for (int i=0; i<possibleMoves.length(); i+=5) {
      String move= possibleMoves.substring(i, i+5);
      makeMove(move);
      flipBoard();
      bestScore= min(bestScore, -minimax(depth-1, YOU));
      flipBoard();
      undoMove(move);
    }
  }
  return bestScore;
}

public static void flipBoard() {
  char temp;
  for (int i=0; i<32; i++) {
    int r= i/8, c= i%8;
    temp= opposite(board[r][c]);
    board[r][c]= opposite(board[7-r][7-c]);
    board[7-r][7-c]= temp;
  }
  int tempK= 63 - kingPosW;
  kingPosW= 63 - kingPosB;
  kingPosB= tempK;
}

boolean runAI= false;
public void mousePressed() {
  int x= mouseX/w;
  int y= mouseY/w;
  int i= index(x, y);
  if (clr(pieceAt(i))==1) selected= i;

  String possibleMoves= possibleMoves();
  choices.clear();
  if (selected!=-1) {
    int sx= selected/8, sy= selected%8;
    for (int j=0; j<possibleMoves.length(); j+=5) {
      String move= possibleMoves.substring(j, j+5);
      if (move.charAt(4)!='$') {
        if (sx==getIntFromString(move, 0) && sy==getIntFromString(move, 1)) choices.append(move);
      } else {
        int y1= getIntFromString(move, 0);
        if (sx==1 && sy==y1) choices.append(move);
      }
    }
  }

  if (selected!=-1 && clr(pieceAt(i))!=YOU) {
    for (String move : choices) {
      if (move.charAt(4)!='$') {
        if (x==getIntFromString(move, 3) && y==getIntFromString(move, 2)) {
          makeMove(move);
          runAI= true;
          flipBoard();
          if (!isKingSafe()) checkSound.play();
          else if (move.charAt(4)==' ') moveSound.play();
          else captureSound.play();
          selected= -1;
          choices.clear();
          flipBoard();
          break;
        }
      } else {
        int x2= getIntFromString(move, 1);
        int y2= 0;
        if (x==x2 && y==y2) {
          makeMove(move);
          runAI= true;
          promotionSound.play();
          selected=-1;
          choices.clear();
          break;
        }
      }
    }
  }
  //println(evaluateBoard());
}
public void drawRect(int c, int i) {
  fill(c);
  rect(i%8 * w, i/8 * w, w, w);
}

public void showPieces() {
  for (int i=0; i<8; i++) {
    for (int j=0; j<8; j++) {
      if (board[i][j] == ' ') continue;
      image(piecesImages.get(board[i][j]), j*w, i*w);
    }
  }
}
public int getIntFromString(String s, int i) {
  return Character.getNumericValue(s.charAt(i));
}
public void printBoard() {
  for (int i=0; i<8; i++) {
    for (int j=0; j<8; j++) {
      print("["+board[i][j]+']');
    }
    print('\n');
  }
}
static class Move {
  int x, y;
  Move(int x, int y) {
    this.x= x;
    this.y= y;
  }
  public Move mult(int n) {
    return new Move(x*n, y*n);
  }
}

static Move[] knightMoves= {
  new Move(1, 2), new Move(1, -2), 
  new Move(-1, 2), new Move(-1, -2), 
  new Move(2, 1), new Move(2, -1), 
  new Move(-2, 1), new Move(-2, -1)
};
static Move[] bishopMoves={
  new Move(1, 1), 
  new Move(1, -1), 
  new Move(-1, 1), 
  new Move(-1, -1)
};
static Move[] rookMoves={
  new Move(0, 1), 
  new Move(0, -1), 
  new Move(1, 0), 
  new Move(-1, 0)
};
static Move[] queenMoves={
  new Move(1, 1), 
  new Move(1, -1), 
  new Move(-1, 1), 
  new Move(-1, -1), 
  new Move(0, 1), 
  new Move(0, -1), 
  new Move(1, 0), 
  new Move(-1, 0)
};
static Move[] kingMoves= queenMoves;

public static String possibleMoves() {
  String list= "";
  for (int i=0; i<8; i++) {
    for (int j=0; j<8; j++) {
      char p= board[i][j];
      switch(p) {
      case 'P':
        list+=pawnMoves(i, j);
        break;
      case 'R':
        list+=rookMoves(i, j);
        break;
      case 'N':
        list+=knightMoves(i, j);
        break;
      case 'B':
        list+=bishopMoves(i, j);
        break;
      case 'Q':
        list+=queenMoves(i, j);
        break;
      case 'K':
        list+=kingMoves(i, j);
        break;
      }
    }
  }
  return list;
}

// Promoting - $
public void makeMove(String move) {
  if (move.charAt(4)!='$') {
    // x1 y1 x2 y2 capturedPiece 
    int x1= getIntFromString(move, 0);
    int y1= getIntFromString(move, 1);
    int x2= getIntFromString(move, 2);
    int y2= getIntFromString(move, 3);

    char me= board[x1][y1];
    board[x2][y2]= me;
    board[x1][y1]= ' ';
    if (board[x2][y2]=='K') {
      kingPosW= index(y2, x2);
    }
    count++;
  } else {
    //promoting pawn
    // c1 c2 capturedPiece promotedPiece $
    int x1= 1, x2= 0;
    int y1= getIntFromString(move, 0);
    int y2= getIntFromString(move, 1);
    board[x2][y2]= move.charAt(3);
    board[x1][y1]= ' ';
  }
}

public void undoMove(String move) {
  if (move.charAt(4)!='$') {
    // x1 y1 x2 y2 capturedPiece 
    int x1= getIntFromString(move, 0);
    int y1= getIntFromString(move, 1);
    int x2= getIntFromString(move, 2);
    int y2= getIntFromString(move, 3);
    char me= board[x2][y2];
    board[x1][y1]= me;
    board[x2][y2]= move.charAt(4);
    if (board[x1][y1]=='K') {
      kingPosW= index(y1, x1);
    }
  } else {
    //promoting pawn
    // c1 c2 capturedPiece promotedPiece P
    int x1= 1, x2= 0;
    int y1= getIntFromString(move, 0);
    int y2= getIntFromString(move, 1);
    board[x2][y2]= move.charAt(2);
    board[x1][y1]= 'P';
  }
}

public static String pawnMoves(int x, int y) {
  String move= "";
  char oldPiece;
  char[] promotions= {'Q', 'N', 'R', 'B'};
  for (int i=-1; i<=1; i+=2) {
    try {
      oldPiece= board[x-1][y+i];
      if (clr(board[x-1][y+i])==-1) {
        board[x][y]= ' ';
        if (x>1) {
          board[x-1][y+i]= 'P';
          if (isKingSafe()) { 
            move= move + x  + y + (x-1) + (y+i) + oldPiece;
          }
        } else {
          for (int k=0; k<promotions.length; k++) {
            board[x-1][y+i]= promotions[k];
            if (isKingSafe()) {
              move= move + y + (y+i) + oldPiece + promotions[k] + '$';
            }
          }
        }
        board[x][y]= 'P';
        board[x-1][y+i]= oldPiece;
      }
    }
    catch(Exception e) {
    }
  }

  try {
    if (board[x-1][y]==' ') {
      board[x][y]= ' ';
      if (x>1) {
        board[x-1][y]= 'P';
        if (isKingSafe()) { 
          move= move + x + y + (x-1) + y + ' ';
        }
      } else {
        for (int k=0; k<promotions.length; k++) {
          board[x-1][y]= promotions[k];
          if (isKingSafe()) {
            move= move + y + y + ' ' + promotions[k] + '$';
          }
        }
      }
      board[x][y]= 'P';
      board[x-1][y]= ' ';
    }
  }
  catch(Exception e) {
  }

  try {
    if (board[x-1][y]==' ' && board[x-2][y]==' ' && x==6) {
      board[x][y]= ' ';
      board[x-2][y]= 'P';
      if (isKingSafe()) {
        move= move + x + y + (x-2) + y + ' ';
      }
      board[x][y]= 'P';
      board[x-2][y]= ' ';
    }
  }
  catch(Exception e) {
  }

  return move;
}
public static String knightMoves(int x, int y) {
  String move= "";
  char oldPiece;
  for (Move mv : knightMoves) {
    try {
      oldPiece= board[x+mv.x][y+mv.y];
      if (clr(oldPiece) != 1) {
        board[x+mv.x][y+mv.y]= 'N';
        board[x][y]= ' ';
        if (isKingSafe()) {
          move= move + x + y + (x+mv.x) + (y+mv.y) + oldPiece;
        }
        board[x][y]= 'N';
        board[x+mv.x][y+mv.y]= oldPiece;
      }
    }
    catch(Exception e) {
    }
  }
  return move;
}
public static String bishopMoves(int x, int y) {
  String move= "";
  char oldPiece;
  for (Move mv : bishopMoves) {
    try {
      int k= 1;
      Move m= mv.mult(k);
      while (clr(board[x+m.x][y+m.y]) != 1) {
        oldPiece= board[x+m.x][y+m.y];
        board[x+m.x][y+m.y]= 'B';
        board[x][y]= ' ';
        if (isKingSafe()) {
          move= move+ x + y + (x+m.x) + (y+m.y) + oldPiece;
        }
        board[x][y]= 'B';
        board[x+m.x][y+m.y]= oldPiece;
        m= mv.mult(++k);
        if (clr(oldPiece)==-1) break;
      }
    }
    catch(Exception e) {
    }
  }
  return move;
}
public static String rookMoves(int x, int y) {
  String move= "";
  char oldPiece;
  for (Move mv : rookMoves) {
    try {
      int k= 1;
      Move m= mv.mult(k);
      while (clr(board[x+m.x][y+m.y]) != 1) {
        oldPiece= board[x+m.x][y+m.y];
        board[x+m.x][y+m.y]= 'R';
        board[x][y]= ' ';
        if (isKingSafe()) {
          move= move+ x + y + (x+m.x) + (y+m.y) + oldPiece;
        }
        board[x][y]= 'R';
        board[x+m.x][y+m.y]= oldPiece;
        m= mv.mult(++k);
        if (clr(oldPiece)==-1) break;
      }
    }
    catch(Exception e) {
    }
  }
  return move;
}
public static String queenMoves(int x, int y) {
  String move= "";
  char oldPiece;
  for (Move mv : queenMoves) {
    try {
      int k= 1;
      Move m= mv.mult(k);
      while (clr(board[x+m.x][y+m.y]) != 1) {
        oldPiece= board[x+m.x][y+m.y];
        board[x+m.x][y+m.y]= 'Q';
        board[x][y]= ' ';
        if (isKingSafe()) {
          move= move+ x + y + (x+m.x) + (y+m.y) + oldPiece;
        }
        board[x][y]= 'Q';
        board[x+m.x][y+m.y]= oldPiece;
        m= mv.mult(++k);
        if (clr(oldPiece)==-1) break;
      }
    }
    catch(Exception e) {
    }
  }
  return move;
}
public static String kingMoves(int x, int y) {
  String move= "";
  char oldPiece;
  for (Move mv : kingMoves) {
    try {
      oldPiece= board[x+mv.x][y+mv.y];
      if (clr(oldPiece) != 1) {
        board[x+mv.x][y+mv.y]= 'K';
        board[x][y]= ' ';
        kingPosW= index(y+mv.y, x+mv.x);
        if (isKingSafe()) {
          move= move+ x + y + (x+mv.x) + (y+mv.y) + oldPiece;
        }
        board[x][y]= 'K';
        board[x+mv.x][y+mv.y]= oldPiece;
        kingPosW= index(y, x);
      }
    }
    catch(Exception e) {
    }
  }

  //add castling
  return move;
}

public static boolean isKingSafe() {
  int i= kingPosW / 8, j= kingPosW % 8;
  for (Move mv : bishopMoves) {
    try {
      int k= 1;
      Move m= mv.mult(k);
      while (board[i+m.x][j+m.y] == ' ') {
        m= mv.mult(++k);
      }

      if (board[i+m.x][j+m.y]=='b' || board[i+m.x][j+m.y]=='q') {
        return false;
      }
    }
    catch(Exception e) {
    }
  }
  for (Move mv : rookMoves) {
    try {
      int k= 1;
      Move m= mv.mult(k);
      while (board[i+m.x][j+m.y] == ' ') {
        m= mv.mult(++k);
      }
      if (board[i+m.x][j+m.y]=='r' || board[i+m.x][j+m.y]=='q') {
        return false;
      }
    }
    catch(Exception e) {
    }
  }
  for (Move mv : kingMoves) {
    try {
      if (board[i+mv.x][j+mv.y]=='k') {
        return false;
      }
    }
    catch(Exception e) {
    }
  }
  for (Move mv : knightMoves) {
    try {
      if (board[i+mv.x][j+mv.y]=='n') {
        return false;
      }
    }
    catch(Exception e) {
    }
  }
  for (int k=-1; k<=1; k+=2) {
    try {
      if (board[i-1][j+k]=='p') {
        return false;
      }
    }
    catch(Exception e) {
    }
  }

  return true;
}
public char pieceAt(int i) {
  return board[i/8][i%8];
}
public static int index(int x, int y) {
  return x + y * 8;
}
public static int clr(char c) {
  if (Character.isLowerCase(c)) return -1;
  if (Character.isUpperCase(c)) return 1;
  return 0;
}
public static char opposite(char c) {
  if (clr(c)==1) return Character.toLowerCase(c);
  else return Character.toUpperCase(c);
}
static int pawnBoard[][]={//attribute to http://chessprogramming.wikispaces.com/Simplified+evaluation+function
  { 0, 0, 0, 0, 0, 0, 0, 0}, 
  {50, 50, 50, 50, 50, 50, 50, 50}, 
  {10, 10, 20, 30, 30, 20, 10, 10}, 
  { 5, 5, 10, 25, 25, 10, 5, 5}, 
  { 0, 0, 0, 20, 20, 0, 0, 0}, 
  { 5, -5, -10, 0, 0, -10, -5, 5}, 
  { 5, 10, 10, -20, -20, 10, 10, 5}, 
  { 0, 0, 0, 0, 0, 0, 0, 0}};
static int rookBoard[][]={
  { 0, 0, 0, 0, 0, 0, 0, 0}, 
  { 5, 10, 10, 10, 10, 10, 10, 5}, 
  {-5, 0, 0, 0, 0, 0, 0, -5}, 
  {-5, 0, 0, 0, 0, 0, 0, -5}, 
  {-5, 0, 0, 0, 0, 0, 0, -5}, 
  {-5, 0, 0, 0, 0, 0, 0, -5}, 
  {-5, 0, 0, 0, 0, 0, 0, -5}, 
  { 0, 0, 0, 5, 5, 0, 0, 0}};
static int knightBoard[][]={
  {-50, -40, -30, -30, -30, -30, -40, -50}, 
  {-40, -20, 0, 0, 0, 0, -20, -40}, 
  {-30, 0, 10, 15, 15, 10, 0, -30}, 
  {-30, 5, 15, 20, 20, 15, 5, -30}, 
  {-30, 0, 15, 20, 20, 15, 0, -30}, 
  {-30, 5, 10, 15, 15, 10, 5, -30}, 
  {-40, -20, 0, 5, 5, 0, -20, -40}, 
  {-50, -40, -30, -30, -30, -30, -40, -50}};
static int bishopBoard[][]={
  {-20, -10, -10, -10, -10, -10, -10, -20}, 
  {-10, 0, 0, 0, 0, 0, 0, -10}, 
  {-10, 0, 5, 10, 10, 5, 0, -10}, 
  {-10, 5, 5, 10, 10, 5, 5, -10}, 
  {-10, 0, 10, 10, 10, 10, 0, -10}, 
  {-10, 10, 10, 10, 10, 10, 10, -10}, 
  {-10, 5, 0, 0, 0, 0, 5, -10}, 
  {-20, -10, -10, -10, -10, -10, -10, -20}};
static int queenBoard[][]={
  {-20, -10, -10, -5, -5, -10, -10, -20}, 
  {-10, 0, 0, 0, 0, 0, 0, -10}, 
  {-10, 0, 5, 5, 5, 5, 0, -10}, 
  { -5, 0, 5, 5, 5, 5, 0, -5}, 
  {  0, 0, 5, 5, 5, 5, 0, -5}, 
  {-10, 5, 5, 5, 5, 5, 0, -10}, 
  {-10, 0, 5, 0, 0, 0, 0, -10}, 
  {-20, -10, -10, -5, -5, -10, -10, -20}};
static int kingMidBoard[][]={
  {-30, -40, -40, -50, -50, -40, -40, -30}, 
  {-30, -40, -40, -50, -50, -40, -40, -30}, 
  {-30, -40, -40, -50, -50, -40, -40, -30}, 
  {-30, -40, -40, -50, -50, -40, -40, -30}, 
  {-20, -30, -30, -40, -40, -30, -30, -20}, 
  {-10, -20, -20, -20, -20, -20, -20, -10}, 
  { 20, 20, 0, 0, 0, 0, 20, 20}, 
  { 20, 30, 10, 0, 0, 10, 30, 20}};
static int kingEndBoard[][]={
  {-50, -40, -30, -20, -20, -30, -40, -50}, 
  {-30, -20, -10, 0, 0, -10, -20, -30}, 
  {-30, -10, 20, 30, 30, 20, -10, -30}, 
  {-30, -10, 30, 40, 40, 30, -10, -30}, 
  {-30, -10, 30, 40, 40, 30, -10, -30}, 
  {-30, -10, 20, 30, 30, 20, -10, -30}, 
  {-30, -30, 0, 0, 0, 0, -30, -30}, 
  {-50, -30, -30, -30, -30, -30, -30, -50}};
static class Rating {
  public static int rating(int list, int depth) {
    int score= 0;
    int material= rateMaterial();
    score+=rateAttack();
    score+=material;
    score+=rateMovability(list, depth, material);
    score+=ratePositional(material);
    flipBoard();
    material= rateMaterial();
    score-=rateAttack();
    score-=material;
    score-=rateMovability(list, depth, material);
    score-=ratePositional(material);
    flipBoard();
    //if(score + depth*50<7800) println(score + depth*50);
    return -(score + depth*50);
  }
  public static int rateAttack() {
    int counter=0;
    int tempPositionC= kingPosW;
    for (int i=0; i<64; i++) {
      switch (board[i/8][i%8]) {
      case 'P': 
        kingPosW=i; 
        if (!isKingSafe()) {
          counter-=64;
        }
        break;
      case 'R': 
        kingPosW=i; 
        if (!isKingSafe()) {
          counter-=500;
        }
        break;
      case 'N': 
        kingPosW=i; 
        if (!isKingSafe()) {
          counter-=300;
        }
        break;
      case 'B': 
        kingPosW=i; 
        if (!isKingSafe()) {
          counter-=300;
        }
        break;
      case 'Q': 
        kingPosW=i; 
        if (!isKingSafe()) {
          counter-=900;
        }
        break;
      }
    }
    kingPosW=tempPositionC;
    if (!isKingSafe()) {
      counter-=200;
    }
    return counter/2;
  }
  public static int rateMaterial() {
    int val= 0, bishops= 0;
    for (int i=0; i<8; i++) {
      for (int j=0; j<8; j++) {
        char p= board[i][j];
        switch(p) {
        case 'P': 
          val+=100; 
          break;
        case 'N': 
          val+=300; 
          break;
        case 'B':
          bishops++;
          break;
        case 'R': 
          val+=500; 
          break;
        case 'Q': 
          val+=900; 
          break;
        }
      }
    }
    if (bishops>=2) val+=300*bishops;
    else if (bishops==1) val+=250;
    return val;
  }
  public static int rateMovability(int listLength, int depth, int material) {
    int counter=0;
    counter+=listLength; //5 pointer per valid move
    if (listLength==0) { //current side is in checkmate or stalemate
      if (!isKingSafe()) { //if checkmate
        counter+= -200000*depth;
      } else { //if stalemate
        counter+= -150000*depth;
      }
    }
    return counter;
  }
  public static int ratePositional(int material) {
    int counter=0;
    for (int i=0; i<64; i++) {
      switch (board[i/8][i%8]) {
      case 'P': 
        counter+=pawnBoard[i/8][i%8];
        break;
      case 'R': 
        counter+=rookBoard[i/8][i%8];
        break;
      case 'N': 
        counter+=knightBoard[i/8][i%8];
        break;
      case 'B': 
        counter+=bishopBoard[i/8][i%8];
        break;
      case 'Q': 
        counter+=queenBoard[i/8][i%8];
        break;
      case 'K': 
        if (material>=1750) {
          counter+=kingMidBoard[i/8][i%8]; 
          counter+=kingMoves(kingPosW/8, kingPosW%8).length()*10;
        } else {
          counter+=kingEndBoard[i/8][i%8]; 
          counter+=kingMoves(kingPosW/8, kingPosW%8).length()*30;
        }
        break;
      }
    }
    return counter;
  }
}
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "Chess_AI" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
