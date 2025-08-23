package org.alnitaka.zenon.mapper;

import java.util.List;
import org.alnitaka.zenon.dto.ProjectDto;
import org.alnitaka.zenon.entity.Project;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProjectMapper {
	Project toEntity(ProjectDto projectDto);

	ProjectDto toDto(Project project);

	List<ProjectDto> toDto(List<Project> project);

	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
	Project partialUpdate(ProjectDto projectDto, @MappingTarget Project project);
}
