package org.example.searxngscrapper.api;

import org.example.searxngscrapper.modal.dto.EvaluationDataDto;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/v0/evaluate")
public class AiEvaluationController {

    @PostMapping
    public EvaluationDataDto evaluateData(@RequestBody EvaluationDataDto evaluationData){

        return new EvaluationDataDto();
    }
}
