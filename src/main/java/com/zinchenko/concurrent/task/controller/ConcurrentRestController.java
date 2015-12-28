package com.zinchenko.concurrent.task.controller;

import com.zinchenko.concurrent.task.nashorn.NashornProcessor;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.*;

@RestController
@RequestMapping(value = "/nashorn")
public class ConcurrentRestController {
    private static final Logger logger = Logger.getLogger(ConcurrentRestController.class);
    @Autowired
    NashornProcessor nashornProcessor;

    @RequestMapping(value = "/eval", method = RequestMethod.POST, produces = "text/plain")
    public DeferredResult<String> addEvalScript(@RequestBody String bodyScript,  HttpServletResponse response){
        ExecutorService executor = Executors.newSingleThreadExecutor();
        DeferredResult<String> deferredResult = new DeferredResult<>();
        FutureTask<String> task = new FutureTask<>(() -> nashornProcessor.addEvalScript(bodyScript));
        executor.execute(task);

        try {
            deferredResult.setResult(task.get(15L, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            logger.warn("InterruptedException:   " + e.getMessage());
            response.setStatus(500);
            deferredResult.setResult("Stoped: InterruptedException");
            return  deferredResult;
        } catch (TimeoutException e) {
            logger.warn("TimeoutException:   " + e.getMessage());
            nashornProcessor.interruptEngine();
            response.setStatus(500);
            deferredResult.setResult("Stoped: TimeoutException");
            return  deferredResult;
        } catch (ExecutionException e) {
            logger.warn("ExecutionException:   " + e.getMessage());
            response.setStatus(500);
            deferredResult.setResult("Stoped: ExecutionException");
            return  deferredResult;
        }finally {
            executor.shutdown();
        }
        if (deferredResult.getResult().toString().equals("503")){
            deferredResult.setResult("Engine is busy");
            response.setStatus(503);
        }
        return deferredResult;
    }


    @RequestMapping(value = "/invoke/{func_name}", method = RequestMethod.POST, produces = "text/plain")
    public DeferredResult<String> invokeFunction(@PathVariable String func_name, HttpServletResponse response){
        String result=null;
        ExecutorService executor = Executors.newSingleThreadExecutor();
        DeferredResult<String> deferredResult = new DeferredResult<>();
        FutureTask<String> task = new FutureTask<>(() -> nashornProcessor.invokeFunction(func_name));
        executor.execute(task);

        try {
            result=task.get(15L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.warn("InterruptedException:   " + e.getMessage());
            response.setStatus(500);
            deferredResult.setResult("Stoped: InterruptedException");
            return  deferredResult;
        } catch (TimeoutException e) {
            logger.warn("TimeoutException:   " + e.getMessage());
            nashornProcessor.interruptEngine();
            response.setStatus(500);
            deferredResult.setResult("Stoped: TimeoutException");
            return  deferredResult;
        } catch (ExecutionException e) {
            logger.warn("ExecutionException:   " + e.getMessage());
            response.setStatus(500);
            deferredResult.setResult("Stoped: ExecutionException");
            return  deferredResult;
        }finally {
            executor.shutdown();
        }

        if (result.equals("503")){
            result = "Engine is busy";
            response.setStatus(503);
        }
        deferredResult.setResult(result);
        return deferredResult;
    }

    @RequestMapping(value = "/getBindings", method = RequestMethod.POST, produces = "text/plain")
    public String getBindings(){
        return nashornProcessor.getBindings();
    }

    @RequestMapping(value = "/killTask", method = RequestMethod.POST, produces = "text/plain")
    public String killTask(HttpServletResponse response){
        response.setStatus(500);
        return nashornProcessor.interruptEngine();
    }

    @RequestMapping(value = "/getStatus", method = RequestMethod.GET, produces = "text/plain")
    public String getStatus(){
        return nashornProcessor.getStatus();
    }

}
