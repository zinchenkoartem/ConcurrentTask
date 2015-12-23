package com.zinchenko.concurrent.task.controller;

import com.zinchenko.concurrent.task.nashorn.NashornProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.*;

@RestController
@RequestMapping(value = "/nashorn")
public class ConcurrentRestController {
    @Autowired
    NashornProcessor nashornProcessor;

    @RequestMapping(value = "/eval", method = RequestMethod.POST, produces = "text/plain")
    public DeferredResult<String> addEvalScript(@RequestBody String bodyScript,  HttpServletResponse response){

        DeferredResult<String> deferredResult = new DeferredResult<>();
        CompletableFuture.supplyAsync(( ) -> nashornProcessor.addEvalScript(bodyScript))
                .whenCompleteAsync((result, throwable) -> deferredResult.setResult(result));

        deferredResult.onTimeout(()->{
            String message= nashornProcessor.interruptEngine();
            response.setStatus(500);
            deferredResult.setResult("Timeout Error.\n"+ message);
        });
        return deferredResult;
    }

    @RequestMapping(value = "/invoke/{func_name}", method = RequestMethod.POST, produces = "text/plain")
    public DeferredResult<String> invokeFunction(@PathVariable String func_name, HttpServletResponse response){

        DeferredResult<String> deferredResult = new DeferredResult<>();
                CompletableFuture.supplyAsync(( ) -> nashornProcessor.invokeFunction(func_name))
                .whenCompleteAsync((result, throwable) -> deferredResult.setResult(result));

        deferredResult.onTimeout(()->{
           String message= nashornProcessor.interruptEngine();
            response.setStatus(500);
            deferredResult.setResult("Timeout Error.\n"+ message);
        });
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
