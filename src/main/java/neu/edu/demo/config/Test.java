package neu.edu.demo.config;

public class Test {

    static void main(){

    }
    public void fissbuss(){
        for (int i = 1; i <= 20; i++){
            if ((i % 3 == 0) && (i % 5 != 0)){
                System.out.println("fiss");
            }else if ((i % 5 == 0) && (i % 3 != 0)){
                System.out.println("buss");
            }else if (( i % 3 == 0) && ( i % 5 == 0)){
                System.out.println("fissbuss");
            }else{
                System.out.println(i);
            }
        }
    }
}
