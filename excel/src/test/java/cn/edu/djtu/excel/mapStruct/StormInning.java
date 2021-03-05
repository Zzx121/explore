package cn.edu.djtu.excel.mapStruct;

/**
 * @author zzx
 * @date 2021/1/6
 * 
 * The restriction on exceptions does not apply to constructors. You can see in Stormylnning that a constructor can throw anything it wants,
 *  * regardless of what the base-class constructor throws.However, since a base-class constructor must always be called 
 *  * one way or another (here, the default constructor is called automatically), the derived-class constructor must declare 
 *  * any base-class constructor exceptions in its exception specification.
 *
 * The last point of interest is in main( ). Here, you can see that if you’re dealing with exactly a StormyInning object, 
 * the compiler forces you to catch only the exceptions that are specific to that class, but if you upcast to the base type, 
 * then the compiler (correctly) forces you to catch the exceptions for the base type. 
 * All these constraints produce much more robust exceptionhandling code.6
 * Although exception specifications are enforced by the compiler during inheritance, 
 * the exception specifications are not part of the type of a method, which comprises only the method name and argument types.
 * Therefore, you cannot overload methods based on exception specifications. 
 * In addition, just because an exception specification exists in a baseclass version of a method doesn’t mean that 
 * it must exist in the derived-class version of the method. This is quite different from inheritance rules,
 * where a method in the base class must also exist in the derived class. Put another way, 
 * the "exception specification interface" for a particular method may narrow during inheritance and overriding,
 * but it may not widen—this is precisely the opposite of the rule for the class interface during inheritance.
 */
class BaseBallException extends Exception {}
class Foul extends BaseBallException {}
class Strike extends BaseBallException {}

abstract class Inning {
    public Inning() throws BaseBallException {}
    public void event() throws BaseBallException {}
    public abstract void atBat() throws Strike, Foul;
    public void walk() {}
}

class StormException extends Exception {}
class RainOut extends StormException {}
class PopFoul extends Foul {}

interface Storm {
    void event() throws RainOut;
    void rainHard() throws RainOut;
}

public class StormInning extends Inning implements Storm {

    public StormInning() throws BaseBallException, RainOut {
    }
    
    public StormInning(String s) throws Foul, BaseBallException {}

    @Override
    public void atBat() throws Strike, PopFoul {
        
    }

    @Override
    public void rainHard() throws RainOut {

    }

    @Override
    public void event() {
        
    }

    public static void main(String[] args) {
        try {
            StormInning si = new StormInning();
            si.atBat();
        } catch (PopFoul e) {
            System.out.println("Pop foul");
        } catch (RainOut e) {
            System.out.println("Rained out");
        } catch (BaseBallException e) {
            System.out.println("Generic baseball exception");
        }

        try {
            Inning inning = new StormInning();
            inning.atBat();
        } catch (BaseBallException e) {
            e.printStackTrace();
        } catch (RainOut rainOut) {
            rainOut.printStackTrace();
        }
    }

}
