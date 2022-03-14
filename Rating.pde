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
  static int rating(int list, int depth) {
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
  static int rateAttack() {
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
  static int rateMaterial() {
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
  static int rateMovability(int listLength, int depth, int material) {
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
  static int ratePositional(int material) {
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
