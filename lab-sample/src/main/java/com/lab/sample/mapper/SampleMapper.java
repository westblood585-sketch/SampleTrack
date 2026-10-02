package com.lab.sample.mapper;

import com.lab.sample.dto.SampleDetailResponse;
import com.lab.sample.dto.SampleSummaryResponse;
import com.lab.sample.dto.SampleTestResponse;
import com.lab.sample.dto.StageHistoryResponse;
import com.lab.sample.dto.TestResultResponse;
import com.lab.sample.entity.Sample;
import com.lab.sample.entity.SampleStageHistory;
import com.lab.sample.entity.SampleTest;
import com.lab.sample.entity.TestDefinition;
import com.lab.sample.entity.TestResult;
import com.lab.sample.util.ResultFlagEvaluator;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = CustomerMapper.class)
public interface SampleMapper {

    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerName", source = "customer.name")
    SampleSummaryResponse toSummary(Sample sample);

    SampleDetailResponse toDetail(Sample sample);

    StageHistoryResponse toHistoryResponse(SampleStageHistory history);

    /** Referans araligina gore bayrak hesabi gerektigi icin elle yazildi. */
    default SampleTestResponse toTestResponse(SampleTest test) {
        TestDefinition definition = test.getTestDefinition();
        TestResult result = test.getResult();
        TestResultResponse resultResponse = result == null ? null : new TestResultResponse(
                result.getId(),
                result.getResultValue(),
                result.getEnteredAt(),
                result.getEnteredBy(),
                ResultFlagEvaluator.evaluate(result.getResultValue(), definition.getRefMin(), definition.getRefMax()));
        return new SampleTestResponse(
                test.getId(),
                definition.getId(),
                definition.getCode(),
                definition.getName(),
                definition.getUnit(),
                definition.getRefMin(),
                definition.getRefMax(),
                test.getStatus(),
                resultResponse);
    }
}
