package di.ex01;

public class LgTV implements TV {
    @Override
    public void powerOn(){
        System.out.println("LG TV: powerOn");
    }

    @Override
    public void powerOff() {
        System.out.println("LG TV: powerOff");
    }

    @Override
    public void volumeUp() {
        System.out.println("LG TV: volumeUp");
    }

    @Override
    public void volumeDown() {
        System.out.println("LG TV: volumeDown");
    }
}
