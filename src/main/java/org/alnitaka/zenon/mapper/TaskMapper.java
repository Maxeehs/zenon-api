package org.alnitaka.zenon.mapper;

import java.util.List;
import org.alnitaka.zenon.dto.TaskDto;
import org.alnitaka.zenon.entity.Task;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface TaskMapper {
	Task toEntity(TaskDto taskDto);

	TaskDto toDto(Task task);

	List<TaskDto> toDto(List<Task> task);

	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
	Task partialUpdate(TaskDto taskDto, @MappingTarget Task task);
}
