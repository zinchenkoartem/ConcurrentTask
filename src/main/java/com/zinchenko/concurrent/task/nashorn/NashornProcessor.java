package com.zinchenko.concurrent.task.nashorn;

import org.springframework.stereotype.Component;

import javax.script.*;
import java.util.concurrent.*;


@Component
public class NashornProcessor {
    private ScriptEngine engine;
    private Thread currThread;
    private Semaphore semaphore;
    private boolean status;

    public NashornProcessor(){
        semaphore = new Semaphore(1);
        engine = new ScriptEngineManager().getEngineByName("nashorn");
    }

    public String addEvalScript(String bodyScript){
        System.out.println("----------------Enter to eval method - "+Thread.currentThread().getName()+"----------------");
        try {
            if (semaphore.tryAcquire(10, TimeUnit.SECONDS)) {
                currThread = Thread.currentThread();
                System.out.println("Enter to semaphore - "+Thread.currentThread().getName());
                    try {
                            status=true;
                            sleep(1000);
//                            ------------------
                             engine.eval(bodyScript);
//                            ------------------
                            status=false;
                    }catch (ScriptException e) {
                        e.printStackTrace();
                        status=false;
                        return e.getMessage();
                    }
            }else {
                return "Engine is busy";
            }
        }catch (InterruptedException e) {
            e.printStackTrace();
            return "Waiting thread was interrupted";
        }finally {
            if (!status) {
                System.out.println("Exit from semaphore -  " + Thread.currentThread().getName());
                semaphore.release();
            }
        }
        System.out.println("----------------Exit from eval method - "+Thread.currentThread().getName()+"----------------");
        System.out.println();
        return "Script  added to Context";
    }

    public String invokeFunction(String func_name){
        System.out.println("----------------Enter to invoke method - "+Thread.currentThread().getName()+"----------------");
        try {
            if (semaphore.tryAcquire(10, TimeUnit.SECONDS)) {
                currThread = Thread.currentThread();
                System.out.println("Enter to semaphore - "+Thread.currentThread().getName());
                Object result;
                Invocable invocable;
                if (engine instanceof Invocable) {
                    invocable = (Invocable) engine;
                    try {
                        if (engine.getBindings(100).get(func_name) != null) {
                            status=true;
                            sleep(3000);
//                            ------------------
                                result = invocable.invokeFunction(func_name);
//                            ------------------
                            status=false;
                            System.out.println("----------------Exit from invoke method - "+Thread.currentThread().getName()+"----------------");
                            System.out.println();
                            return result.toString();
                            }
                        }catch (ScriptException | NoSuchMethodException e) {
                            e.printStackTrace();
                            status=false;
                            return e.getMessage();
                        }
                    }
                }else {
                     return "Engine is busy";
                }
            }catch (InterruptedException e) {
                e.printStackTrace();
                return "Waiting thread was interrupted";
            }finally {
            if (!status) {
                System.out.println("Exit from semaphore  -  " + Thread.currentThread().getName());
                semaphore.release();
                }
            }
        return  "Function "+func_name+" is absent";
    }

    public String interruptEngine(){
//        Not work
//        if (status) {
//            currThread.interrupt();
//            future.cancel(true);
//            pool.shutdown();
//            semaphore.release();
//            status=false;
//            return "Thread was interrupted";
//        }
//        return "Nothing to interrupt. Engine ready to use";
        return "Not work";
    }

    public String getStatus(){
       if( semaphore.availablePermits()==0){
           return  "Nashorn is working.";
       }else {
            return "Nashorn is ready to use.";
        }
    }

    public String getBindings(){
        return engine.getBindings(100).entrySet().toString();
    }

    private String sleep(int millis){
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("---interrupt curr sleep thread: "+Thread.currentThread().getName());
            return e.getMessage();
        }
        return "Ok";
    }
}
