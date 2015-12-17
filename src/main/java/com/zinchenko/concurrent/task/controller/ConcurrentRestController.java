package com.zinchenko.concurrent.task.controller;

import com.zinchenko.concurrent.task.nashorn.NashornProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.Date;
import java.util.concurrent.*;

@RestController
@RequestMapping(value = "/nashorn")
public class ConcurrentRestController {
    @Autowired
    NashornProcessor nashornProcessor;

    @RequestMapping(value = "/addScript", method = RequestMethod.POST, produces = "text/plain")
    public DeferredResult<String> addEvalScript(@RequestBody String bodyScript){
        System.out.println(new Date(System.currentTimeMillis())+"--start servlet-method--");

        DeferredResult<String> deferredResult = new DeferredResult<>();
        CompletableFuture.supplyAsync(( ) -> nashornProcessor.addEvalScript(bodyScript))
                .whenCompleteAsync((result, throwable) -> deferredResult.setResult(result));

        System.out.println(new Date(System.currentTimeMillis())+"--return servlet-method--");
        return deferredResult;
    }

    @RequestMapping(value = "/invoke/{func_name}", method = RequestMethod.POST, produces = "text/plain")
    public DeferredResult<String> invokeFunction(@PathVariable String func_name){

        DeferredResult<String> deferredResult = new DeferredResult<>();
        CompletableFuture.supplyAsync(( ) -> nashornProcessor.invokeFunction(func_name))
                .whenCompleteAsync((result, throwable) -> deferredResult.setResult(result));

        return deferredResult;
    }

    @RequestMapping(value = "/getBindings", method = RequestMethod.POST, produces = "text/plain")
    public DeferredResult<String> getBindingsl(){
        DeferredResult<String> deferredResult = new DeferredResult<>();
        CompletableFuture.supplyAsync(nashornProcessor::getBindings)
                .whenCompleteAsync((result, throwable) -> deferredResult.setResult(result));
        return deferredResult;
    }

    @RequestMapping(value = "/killTask", method = RequestMethod.POST, produces = "text/plain")
    public String killTask(){
        return nashornProcessor.interruptEngine();
    }

    @RequestMapping(value = "/getStatus", method = RequestMethod.GET, produces = "text/plain")
    public String getStatus(){
        return nashornProcessor.getStatus();
    }

}
