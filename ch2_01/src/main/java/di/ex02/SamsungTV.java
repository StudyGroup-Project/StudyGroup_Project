package di.ex01;

public class SamsungTV implements TV{
    @Override
    public void powerOn(){
        System.out.println("삼성 TV: powerOn");
    }

    @Override
    public void powerOff() {
        System.out.println("삼성 TV: powerOff");
    }

    @Override
    public void volumeUp() {
        System.out.println("삼성 TV: volumeUp");
    }

    @Override
    public void volumeDown() {
        System.out.println("삼성 TV: volumeDown");
    }

}
