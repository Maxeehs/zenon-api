package org.alnitaka.zenon.mapper;

import java.util.List;
import org.alnitaka.zenon.dto.ClientDto;
import org.alnitaka.zenon.entity.Client;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
	unmappedTargetPolicy = ReportingPolicy.IGNORE,
	componentModel = MappingConstants.ComponentModel.SPRING,
	uses = {
		UserMapper.class
	})
public interface ClientMapper {
	Client toEntity(ClientDto clientDto);

	ClientDto toDto(Client client);

	List<ClientDto> toDto(List<Client> client);

	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
	Client partialUpdate(ClientDto clientDto, @MappingTarget Client client);
}
