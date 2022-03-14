static class Move {
  int x, y;
  Move(int x, int y) {
    this.x= x;
    this.y= y;
  }
  Move mult(int n) {
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

static String possibleMoves() {
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
void makeMove(String move) {
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

void undoMove(String move) {
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

static String pawnMoves(int x, int y) {
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
static String knightMoves(int x, int y) {
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
static String bishopMoves(int x, int y) {
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
static String rookMoves(int x, int y) {
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
static String queenMoves(int x, int y) {
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
static String kingMoves(int x, int y) {
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

static boolean isKingSafe() {
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
char pieceAt(int i) {
  return board[i/8][i%8];
}
static int index(int x, int y) {
  return x + y * 8;
}
static int clr(char c) {
  if (Character.isLowerCase(c)) return -1;
  if (Character.isUpperCase(c)) return 1;
  return 0;
}
static char opposite(char c) {
  if (clr(c)==1) return Character.toLowerCase(c);
  else return Character.toUpperCase(c);
}
