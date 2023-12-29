import java.util.*;

public final class ChessMap {
    public static final int e =0;
    public static final int w =1;
    public static final int b =2;
//    public static long cnt=0;
    public ArrayList<Node> nodes=new ArrayList<>(400);
    public int[][] chessMap =new int[20][20];
    public int[][] chessMapCount=new int[20][20];
    public boolean aiIsStop=false;
    private static final int[][] nextXY =new int[][]{{-1,-1},{-1,0},{-1,1}, {0,-1}, {0,1}, {1,-1}, {1,0}, {1,1}}; //搜索用的private static final int[][] dxdy
    private static final long[] value=new long[9];
    private final long[] lineEvalueSum=new long[20];//Use Index:[1,19]
    private final long[] cowEvalueSum=new long[20];//Use Index:[1,19]
    private final long[] leftEvlueSum=new long[38];
    //Use Index:5<x+y-1<33 besides [1,5]+[33,37] is always ZERO
    private final long[] rightEvalueSum=new long[38];
    private static final long INF=Long.MAX_VALUE>>1;
    private int AiColor;

    private PriorityQueue<NodeFlag> allFlag() {
        // 创建一个优先队列，用于存储NodeFlag对象
        PriorityQueue<NodeFlag> flags = new PriorityQueue<>(90, Comparator.comparingInt(NodeFlag::getFlag).reversed());
        // 使用LinkedList获取最后一个和倒数第二个节点
        LinkedList<Node> nodeList = new LinkedList<>(nodes);
        Node a= nodes.get(nodes.size()-1);
        Node b=null;
        if(nodes.size()>1)
            b=nodes.get(nodes.size()-2);
        for(int i = 1; i <= 19; i++) {
            for(int j = 1; j <= 19; j++) {
                if(chessMap[i][j] == e && chessMapCount[i][j] != 0) {
                    NodeFlag flag = null;
                    // 计算位置优先级
                    if(Math.abs(a.x - i) <= 2 && Math.abs(a.y - j) <= 2) {
                        flag = new NodeFlag(i, j, chessMapCount[i][j] + 10);
                    } else if(b != null && Math.abs(b.x - i) <= 2 && Math.abs(b.y - j) <= 2) {
                        flag = new NodeFlag(i, j, chessMapCount[i][j] + 10);
                    } else {
                        flag = new NodeFlag(i, j, chessMapCount[i][j]);
                    }

                    // 如果优先队列未满，则直接插入
                    if(flags.size() < 90) {
                        flags.offer(flag);
                    } else {
                        // 如果优先队列已满，且当前元素大于队列最小元素，则删除队列最小元素并插入当前元素
                        if(flag.compareTo(flags.peek()) > 0) {
                            flags.poll();
                            flags.offer(flag);
                        }
                    }
                }
            }
        }

        return flags;
    }

    public void updataFlag(int x,int y,int cnt){//更新棋盘每个位置
        for(int i=x-3;i<=x+3;i++){
            for(int j=y-3;j<=y+3;j++){
                if(!(x >= 1 && x <= 19 && y >= 1 && y <= 19)) continue;
               chessMapCount[i][j]+=cnt*(4-Math.max(Math.abs(x-i),Math.abs(y-j)));
            }
        }
    }
    boolean canDrop(int x,int y){
        return chessMap[x][y] != e;
    }
    public int lastChessColor(){//计算上次落子颜色
        if(nodes.size()<=1)
            return b;
        return (nodes.size()/2&1)==1? w : b;
    }
    public int nextChessColor(){//计算下次落子颜色
        if(nodes.isEmpty())
            return b;
        return ((nodes.size()+1)/2&1)==1? w : b;
    }
    public int thisChessColor(int i){//第i个棋子颜色
        if(i<=1)
            return b;
        return (i/2&1)==1? w : b;
    }

    public boolean isDropSuccess(int x,int y){
        for (int i=0;i<4;i++) {
            if(continuousSameColor(x,y, nextXY[i][0], nextXY[i][1])+
                    continuousSameColor(x,y,-nextXY[i][0],-nextXY[i][1])+1>=6){
                return true;
            }
        }
        return false;
    }
    public int continuousSameColor(int x,int y,int dx,int dy){
        int a,b,cnt=0;
        a=x+dx;
        b=y+dy;
        while(a >= 1 && a <= 19 && b >= 1 && b <= 19&&chessMap[a][b]==chessMap[x][y]){
            ++cnt;
            a+=dx;
            b+=dy;
        }
        return cnt;
    }
     long getLineEvalue(int x,int mainChessColor){
        StringBuilder s=new StringBuilder();
        int start=1;
        int cnt=0;
        while(start<=19){
            if(chessMap[x][start]!= e)
                break;
            ++start;
            ++cnt;
        }
        //ai ui
        if(cnt==1){
            s.append('0');
        }else if(cnt==2){
            s.append("00");
        }else if(cnt==3){
            s.append("000");
        }else if(cnt>3){
            s.append("0000");
        }
        int end=19;
        cnt=0;
        while(end>=1){
            if(chessMap[x][end]!= e)
                break;
            --end;
            ++cnt;
        }
        for(int j=start;j<=end;j++){
            if(chessMap[x][j]== w)
                s.append('1');
            else if(chessMap[x][j]== b)
                s.append('2');
            else
                s.append('0');
        }
        if(cnt==1){
            s.append('0');
        }else if(cnt==2){
            s.append("00");
        }else if(cnt==3){
            s.append("000");
        }else if(cnt>3){
            s.append("0000");
        }
        return singleValue(s.toString(),mainChessColor);
    }
    private long getCowEvalue(int y,int mainChessColor){
        StringBuilder s=new StringBuilder();
        int start=1;
        int cnt=0;
        while(start<=19){
            if(chessMap[start][y]!= e)
                break;
            ++start;
            ++cnt;
        }
        if(cnt==1){
            s.append('0');
        }else if(cnt==2){
            s.append("00");
        }else if(cnt==3){
            s.append("000");
        }else if(cnt>3){
            s.append("0000");
        }
        int end=19;
        cnt=0;
        while(end>=1){
            if(chessMap[end][y]!= e)
                break;
            --end;
            ++cnt;
        }
        for(int i=start;i<=end;i++){
            if(chessMap[i][y]== w)
                s.append('1');
            else if(chessMap[i][y]== b)
                s.append('2');
            else
                s.append('0');
        }
        if(cnt==1){
            s.append('0');
        }else if(cnt==2){
            s.append("00");
        }else if(cnt==3){
            s.append("000");
        }else if(cnt>3){
            s.append("0000");
        }
        return singleValue(s.toString(),mainChessColor);
    }
    private long getLeftSlantEvalue(int x,int y,int mainChessColor){
        StringBuilder s=new StringBuilder();
        int start_x,start_y;//左下到右上
        if(x+y-1<=19){
            start_x=x+y-1;
            start_y=1;
        }else{
            start_x=19;
            start_y=y+x-19;
        }
        int cnt=0;
        while (start_x>=1&&start_y<=19){
            if(chessMap[start_x][start_y]!= e)
                break;
            --start_x;
            ++start_y;
            ++cnt;
        }
        if(cnt==1){
            s.append('0');
        }else if(cnt==2){
            s.append("00");
        }else if(cnt==3){
            s.append("000");
        }else if(cnt>3){
            s.append("0000");
        }
        int end_x,end_y;
        if(x+y-1<=19){
            end_x=1;
            end_y=x+y-1;
        }else{
            end_x=x+y-19;
            end_y=19;
        }
        cnt=0;
        while(end_x<=19&&end_y>=1){
            if(chessMap[end_x][end_y]!= e)
                break;
            ++end_x;
            --end_y;
            ++cnt;
        }
        for(int i=start_x,j=start_y;i>=end_x&&j<=end_y;--i,++j){
            if(chessMap[i][j]== w)
                s.append('1');
            else if(chessMap[i][j]== b)
                s.append('2');
            else
                s.append('0');
        }
        if(cnt==1){
            s.append('0');
        }else if(cnt==2){
            s.append("00");
        }else if(cnt==3){
            s.append("000");
        }else if(cnt>3){
            s.append("0000");
        }
        return singleValue(s.toString(),mainChessColor);
    }
    private long getRightSlantEvalue(int x,int y,int mainChessColor){
        StringBuilder s=new StringBuilder();
        int start_x,start_y;//左上到右下
        int cnt=0;
        if(x<=y){
            start_x=1;
            start_y=y-x+1;
        }else {
            start_x=x-y+1;
            start_y=1;
        }
        while (start_x<=19&&start_y<=19){
            if(chessMap[start_x][start_y]!= e)
                break;
            ++start_x;
            ++start_y;
            ++cnt;
        }//
        if(cnt==1){
            s.append('0');
        }else if(cnt==2){
            s.append("00");
        }else if(cnt==3){
            s.append("000");
        }else if(cnt>3){
            s.append("0000");
        }
        int end_x,end_y;
        if(x<=y){
            end_x=x+19-y;
            end_y=19;
        }else {
            end_x=19;
            end_y=19-x+y;
        }
        cnt=0;
        while(end_x>=1&&end_y>=1){
            if(chessMap[end_x][end_y]!= e)
                break;
            --end_x;
            --end_y;
            ++cnt;
        }
        for(int i=start_x,j=start_y;i<=end_x&&j<=end_y;i++,++j){
            if(chessMap[i][j]== w)
                s.append('1');
            else if(chessMap[i][j]== b)
                s.append('2');
            else
                s.append('0');
        }
        if(cnt==1){
            s.append('0');
        }else if(cnt==2){
            s.append("00");
        }else if(cnt==3){
            s.append("000");
        }else if(cnt>3){
            s.append("0000");
        }
        return singleValue(s.toString(),mainChessColor);
    }
    private long singleValue(String str,int mainChessColor){
        int otherChessColor=(mainChessColor== w)? b : w;
        return singleValue(str,0,mainChessColor)-(long)(1.1*singleValue(str,0,otherChessColor));
    }
    private long singleValue(String str, int valuestart, int mainChessColor){
        if(str.length()<6)
            return 0;
        int[] n= calcValue(str, valuestart,mainChessColor);
        long ans=0;
        if(n!=null)
             ans+=singleValue(str.substring(0,n[0]), valuestart +1,mainChessColor)+
                singleValue(str.substring(n[1]),0,mainChessColor)+value[n[2]];
        return ans;
    }
    private int[] calcValue(String str, int valuestart, int mainChessColor){//返回值 1:index 2：index+length 3：firstvalue
        int temp;
        if(mainChessColor== w){
            for(int i = valuestart; i<chessValueAndState.whiteChessType.length; i++){
                for(String ss:chessValueAndState.whiteChessType[i]){
                    temp=str.indexOf(ss);
                    if(temp!=-1)
                        return new int[]{temp,temp+ss.length(),i};
                }
            }
        }else{
            for(int i = valuestart; i<chessValueAndState.blackChessType.length; i++){
                for(String ss:chessValueAndState.blackChessType[i]){
                    temp=str.indexOf(ss);
                    if(temp!=-1)
                        return new int[]{temp,temp+ss.length(),i};
                }
            }
        }
        return null;
    }
    private long evalue(int mainChessColor){
        long ans=0;
        for(int i=1;i<=19;i++){
            lineEvalueSum[i]=getLineEvalue(1,mainChessColor);
            ans+=lineEvalueSum[i];
            cowEvalueSum[i]=getCowEvalue(1,mainChessColor);
            ans+=cowEvalueSum[i];
        }
        for(int j=6;j<=19;j++){
            leftEvlueSum[j]=getLeftSlantEvalue(1,j,mainChessColor);
            ans+=leftEvlueSum[j];
        }
        for(int i=2;i<=14;i++){
            leftEvlueSum[i+19-1]=getLeftSlantEvalue(i,19,mainChessColor);
            ans+=leftEvlueSum[i+19-1];
        }
        for(int i=14;i>=1;i--){
            rightEvalueSum[20-i]=getRightSlantEvalue(i,1,mainChessColor);
            ans+=rightEvalueSum[20-i];
        }
        for(int j=2;j<=19;j++){
            rightEvalueSum[18+j]=getRightSlantEvalue(1,j,mainChessColor);
            ans+=rightEvalueSum[18+j];
        }
        return ans;
    }
    private long beta(int depth,long alpha,long beta,long valueSum,int mainChessColor,ArrayList<NodeFlag> flags){
        if (depth == 0) {
            return valueSum;
        }
        NodeFlag a,b;
        long value;
        int nextColor=(mainChessColor == w) ? ChessMap.b : w;
        int size=flags.size();
        long temp_a,temp_aa;//old new
        long temp_b,temp_bb;
        long[][] Old=new long[2][4];
        long[][] New=new long[2][4];
        for(int i=0;i<size;i++){
            a=flags.get(i);
            if(canDrop(a.x, a.y))
                continue;
            Old[0][0]=lineEvalueSum[a.x];
            Old[0][1]=cowEvalueSum[a.y];
            Old[0][2]=leftEvlueSum[a.x+a.y-1];
            if(a.x>=a.y)
                Old[0][3]=rightEvalueSum[20-(a.x-a.y+1)];
            else
                Old[0][3]=rightEvalueSum[38-(20-(a.y-a.x+1))];
            chessMap[a.x][a.y]=mainChessColor;
            if(isDropSuccess(a.x,a.y)){
                chessMap[a.x][a.y]= e;
                return -INF;
            }
            New[0][0]=getLineEvalue(a.x,AiColor);
            New[0][1]=getCowEvalue(a.y,AiColor);
            New[0][2]=getLeftSlantEvalue(a.x,a.y,AiColor);
            New[0][3]=getRightSlantEvalue(a.x,a.y,AiColor);
            temp_a=Old[0][0]+Old[0][1]+Old[0][2]+Old[0][3];
            valueSum-=temp_a;
            temp_aa=New[0][0]+New[0][1]+New[0][2]+New[0][3];
            valueSum+=temp_aa;
            for(int j=i+1;j<size;j++){
                b=flags.get(j);
                if(canDrop(b.x, b.y))
                    continue;
                if(aiIsStop){
                    chessMap[a.x][a.y] = e;
                    return 0;
                }
                Old[1][0]=lineEvalueSum[b.x];
                Old[1][1]=cowEvalueSum[b.y];
                Old[1][2]=leftEvlueSum[b.x+b.y-1];
                if(b.x>=b.y)
                    Old[1][3]=rightEvalueSum[20-(b.x-b.y+1)];
                else
                    Old[1][3]=rightEvalueSum[38-(20-(b.y-b.x+1))];
                chessMap[b.x][b.y]=mainChessColor;
                New[1][0]=getLineEvalue(b.x,AiColor);
                New[1][1]=getCowEvalue(b.y,AiColor);
                New[1][2]=getLeftSlantEvalue(b.x,b.y,AiColor);
                New[1][3]=getRightSlantEvalue(b.x,b.y,AiColor);
                temp_b=Old[1][0]+Old[1][1]+Old[1][2]+Old[1][3];
                valueSum-=temp_b;
                temp_bb=New[1][0]+New[1][1]+New[1][2]+New[1][3];
                valueSum+=temp_bb;
                if(!isDropSuccess(b.x,b.y)){
                    value=alpha(depth-1,alpha,beta,valueSum,nextColor,flags)-chessMapCount[a.x][a.y]-chessMapCount[b.x][b.y];
                }else{
                    value=-INF;
                }
                chessMap[b.x][b.y] = e;
                valueSum-=temp_bb;
                valueSum+=temp_b;
                lineEvalueSum[b.x]=Old[1][0];
                cowEvalueSum[b.y]=Old[1][1];
                leftEvlueSum[b.x+b.y-1]=Old[1][2];
                if(b.x>=b.y)
                    rightEvalueSum[20-(b.x-b.y+1)]=Old[1][3];
                else
                    rightEvalueSum[38-(20-(b.y-b.x+1))]=Old[1][3];
                if(value<beta)
                    beta=value;
                if(alpha>=beta){
                    chessMap[a.x][a.y]= e;
                    lineEvalueSum[a.x]=Old[0][0];
                    cowEvalueSum[a.y]=Old[0][1];
                    leftEvlueSum[a.x+a.y-1]=Old[0][2];
                    if(a.x>=a.y)
                        rightEvalueSum[20-(a.x-a.y+1)]=Old[0][3];
                    else
                        rightEvalueSum[38-(20-(a.y-a.x+1))]=Old[0][3];
                    return beta;
                }
            }
            chessMap[a.x][a.y] = e;
            valueSum-=temp_aa;
            valueSum+=temp_a;
            lineEvalueSum[a.x]=Old[0][0];
            cowEvalueSum[a.y]=Old[0][1];
            leftEvlueSum[a.x+a.y-1]=Old[0][2];
            if(a.x>=a.y)
                rightEvalueSum[20-(a.x-a.y+1)]=Old[0][3];
            else
                rightEvalueSum[38-(20-(a.y-a.x+1))]=Old[0][3];
        }
        return beta;
    }
    private long alpha(int depth,long alpha,long beta,long valueSum,int mainChessColor,ArrayList<NodeFlag> flags) {
//        ++cnt;
        return valueSum;//tuo depth;
    }
    public ArrayList<Node> maxMin(int depth,int mainChessColor){
        long alpha=-INF;
        long beta=INF;
        AiColor=mainChessColor;
        ArrayList<Node> bestNode=new ArrayList<>();
        PriorityQueue<NodeFlag> flagsQueue = allFlag();
        ArrayList<NodeFlag> flags = new ArrayList<>(flagsQueue);
        long valueSum=evalue(AiColor);
        long value = 0;
        int nextColor=mainChessColor== w ? b : w;
        int size=flags.size();
        NodeFlag a,b;
        long temp_a,temp_aa;//old new
        long temp_b,temp_bb;
        long[][] Old=new long[2][4];
        long[][] New=new long[2][4];
        for(int i=0;i<size;i++){
            a=flags.get(i);
            if(canDrop(a.x, a.y))
                continue;
            Old[0][0]=lineEvalueSum[a.x];
            Old[0][1]=cowEvalueSum[a.y];
            Old[0][2]=leftEvlueSum[a.x+a.y-1];
            if(a.x>=a.y)
                Old[0][3]=rightEvalueSum[20-(a.x-a.y+1)];
            else
                Old[0][3]=rightEvalueSum[38-(20-(a.y-a.x+1))];
            chessMap[a.x][a.y]=mainChessColor;
            if (isDropSuccess(a.x, a.y)) {
                chessMap[a.x][a.y] = e;
                bestNode.clear();
                bestNode.add(new Node(a.x,a.y));
                return bestNode;
            }
            New[0][0]=getLineEvalue(a.x,AiColor);
            New[0][1]=getCowEvalue(a.y,AiColor);
            New[0][2]=getLeftSlantEvalue(a.x,a.y,AiColor);
            New[0][3]=getRightSlantEvalue(a.x,a.y,AiColor);
            temp_a=Old[0][0]+Old[0][1]+Old[0][2]+Old[0][3];
            valueSum-=temp_a;
            temp_aa=New[0][0]+New[0][1]+New[0][2]+New[0][3];
            valueSum+=temp_aa;
            for(int j=i+1;j<size;j++){
                b=flags.get(j);
                if (canDrop(b.x, b.y))
                    continue;
                if(aiIsStop){
                    chessMap[a.x][a.y] = e;
                    return null;
                }
                Old[1][0]=lineEvalueSum[b.x];
                Old[1][1]=cowEvalueSum[b.y];
                Old[1][2]=leftEvlueSum[b.x+b.y-1];
                if(b.x>=b.y)
                    Old[1][3]=rightEvalueSum[20-(b.x-b.y+1)];
                else
                    Old[1][3]=rightEvalueSum[38-(20-(b.y-b.x+1))];
                chessMap[b.x][b.y]=mainChessColor;
                New[1][0]=getLineEvalue(b.x,AiColor);
                New[1][1]=getCowEvalue(b.y,AiColor);
                New[1][2]=getLeftSlantEvalue(b.x,b.y,AiColor);
                New[1][3]=getRightSlantEvalue(b.x,b.y,AiColor);
                temp_b=Old[1][0]+Old[1][1]+Old[1][2]+Old[1][3];
                valueSum-=temp_b;
                temp_bb=New[1][0]+New[1][1]+New[1][2]+New[1][3];
                valueSum+=temp_bb;
                if (!isDropSuccess(b.x, b.y)) {
                    value=beta(depth-1,alpha,beta,valueSum,nextColor,flags)+chessMapCount[a.x][a.y]+chessMapCount[b.x][b.y];
                }else{
                    chessMap[a.x][a.y] = e;
                    chessMap[b.x][b.y] = e;
                    bestNode.clear();
                    bestNode.add(new Node(a.x,a.y));
                    bestNode.add(new Node(b.x,b.y));
                    return bestNode;
                }
                chessMap[b.x][b.y] = e;
                valueSum-=temp_bb;
                valueSum+=temp_b;
                lineEvalueSum[b.x]=Old[1][0];
                cowEvalueSum[b.y]=Old[1][1];
                leftEvlueSum[b.x+b.y-1]=Old[1][2];
                if(b.x>=b.y)
                    rightEvalueSum[20-(b.x-b.y+1)]=Old[1][3];
                else
                    rightEvalueSum[38-(20-(b.y-b.x+1))]=Old[1][3];
                if(value==alpha){
                    bestNode.add(new Node(a.x,a.y));
                    bestNode.add(new Node(b.x,b.y));
                }
                if(value>alpha){
                    alpha=value;
                    bestNode.clear();
                    bestNode.add(new Node(a.x,a.y));
                    bestNode.add(new Node(b.x,b.y));
                }
            }
            chessMap[a.x][a.y] = e;
            valueSum-=temp_aa;
            valueSum+=temp_a;
            lineEvalueSum[a.x]=Old[0][0];
            cowEvalueSum[a.y]=Old[0][1];
            leftEvlueSum[a.x+a.y-1]=Old[0][2];
            if(a.x>=a.y)
                rightEvalueSum[20-(a.x-a.y+1)]=Old[0][3];
            else
                rightEvalueSum[38-(20-(a.y-a.x+1))]=Old[0][3];
        }
        int rand= new Random().nextInt(bestNode.size()/2);
        ArrayList<Node> temp=new ArrayList<>();
        temp.add(bestNode.get(rand*2));
        temp.add(bestNode.get(rand*2+1));
        return temp;
    }
}