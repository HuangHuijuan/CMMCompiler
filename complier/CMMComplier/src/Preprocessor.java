import com.sun.imageio.plugins.common.InputStreamAdapter;

import java.io.*;

/**
 * Created by Huang on 2016/1/4.
 */
public class Preprocessor {

    public String preprocess(String text)throws IOException{
       int commentNum=0;
       int type=-1;
       final int SINGLELINE = 0;
       final int MULTILINE = 1;
       char[] prog;
        StringBuilder content = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream((text.getBytes()))));
        String line = "";
        while ((line = reader.readLine()) != null ) {
            prog = line.toCharArray();
            //System.out.println(prog);
            for(int i=0; i<prog.length; i++){
                char c = prog[i];
                if(c=='/'&&type!=SINGLELINE) {
                    if(i<prog.length-1) {
                        c=prog[++i];
                       // System.out.print(c);
                        if (c == '/') {
                            commentNum++;
                            type = SINGLELINE;

                        } else if (c == '*') {
                            commentNum++;
                            type = MULTILINE;
                        } else {
                            i--;
                            c = prog[i];
                        }
                    }
                }
                else if(c=='*'){
                    if(i<prog.length-1) {
                        c = prog[++i];
                       // System.out.print(c);
                        if (c == '/') {
                            commentNum--;
                            continue;
                        }
                        else
                            i--;
                            c = prog[i];
                    }
                }
                if(i==prog.length-1&&type==SINGLELINE) {
                    commentNum--;
                    type =-1;
                    continue;
                }
                if(commentNum==0)
                    content.append(c);
            }
            content.append("\n");
        }

        return content.toString();
    }

    public static void main(String[] args) throws Exception {
        //System.out.print(Preprocessor.preprocess("C:\\Users\\Huang\\Desktop\\测试脚本\\error3_comment.cmm"));
        //Preprocessor.checkComment();
    }

}
