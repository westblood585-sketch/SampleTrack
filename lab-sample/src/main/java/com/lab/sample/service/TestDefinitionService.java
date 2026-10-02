package com.lab.sample.service;

import com.lab.sample.dto.TestDefinitionRequest;
import com.lab.sample.dto.TestDefinitionResponse;

import java.util.List;

public interface TestDefinitionService {

    TestDefinitionResponse create(TestDefinitionRequest request);

    /** Sonucu girilmis bir test tanimi degistirilemez (is kurali 4). */
    TestDefinitionResponse update(Long id, TestDefinitionRequest request);

    /** Aktif/pasif bilgisi katalog erisilebilirligidir; tanim icerigini degistirmez, kilitten etkilenmez. */
    TestDefinitionResponse setActive(Long id, boolean active);

    TestDefinitionResponse get(Long id);

    List<TestDefinitionResponse> list(boolean onlyActive);
}
