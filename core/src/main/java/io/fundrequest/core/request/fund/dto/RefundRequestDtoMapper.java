package io.fundrequest.core.request.fund.dto;

import io.fundrequest.common.infrastructure.mapping.BaseMapper;
import io.fundrequest.common.infrastructure.mapping.DefaultMappingConfig;
import io.fundrequest.core.request.fund.domain.RefundRequest;
import org.mapstruct.Mapper;

@Mapper(config = DefaultMappingConfig.class)
public interface RefundRequestDtoMapper extends BaseMapper<RefundRequest, RefundRequestDto> {

}
