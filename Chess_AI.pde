import processing.sound.SoundFile;

final int w= 80;
PImage boardImage;
final char[] sequence= {'p', 'n', 'b', 'r', 'q', 'k', 'P', 'N', 'B', 'R', 'Q', 'K'};
HashMap<Character, PImage> piecesImages;
static char[][] board;
final int YOU= 1;
final int AI= -YOU;
final int AILevel= 1;
static int kingPosB, kingPosW;
int selected= -1;
StringList choices= new StringList();
int gameWon= 0;
color selectColor, choiceColor, dangerColor, promotionColor;
SoundFile moveSound, captureSound, checkSound, promotionSound;
void settings() {
  size(w*8, w*8);
}

void setup() {
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

  selectColor= #0092FF;
  choiceColor= #FFE600;
  dangerColor= #FC1831;
  promotionColor= #F10AFF;

  moveSound= new SoundFile(this, "sfx/move.mp3");
  captureSound= new SoundFile(this, "sfx/capture.mp3");
  checkSound= new SoundFile(this, "sfx/check.mp3");
  promotionSound= new SoundFile(this, "sfx/promotion.mp3");
}
int t= 0;
void draw() {
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
    text(gameWon==1 ? "White" : "Black", w*4, w*3.2);
    textSize(w*1.1);
    text("Wins", w*4, w*4.7);
  }
}
int count=0;
void runAIMove() {
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
String randomMove() {
  String possibleMoves= possibleMoves();
  int i= int(random(possibleMoves.length()/5))*5;
  return possibleMoves.substring(i, i+5);
}

String goodMove() {
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

String bestMove() {
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
int minimax(int depth, int player) {
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

static void flipBoard() {
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
void mousePressed() {
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

void drawRect(color c, int i) {
  fill(c, 120);
  rect(i%8 * w, i/8 * w, w, w);
}

void showPieces() {
  for (int i=0; i<8; i++) {
    for (int j=0; j<8; j++) {
      if (board[i][j] == ' ') continue;
      image(piecesImages.get(board[i][j]), j*w, i*w);
    }
  }
}
int getIntFromString(String s, int i) {
  return Character.getNumericValue(s.charAt(i));
}
void printBoard() {
  for (int i=0; i<8; i++) {
    for (int j=0; j<8; j++) {
      print("["+board[i][j]+']');
    }
    print('\n');
  }
}
