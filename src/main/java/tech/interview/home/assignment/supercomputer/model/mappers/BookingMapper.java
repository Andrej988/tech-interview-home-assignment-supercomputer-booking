package tech.interview.home.assignment.supercomputer.model.mappers;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import tech.interview.home.assignment.supercomputer.model.domain.Booking;
import tech.interview.home.assignment.supercomputer.model.dto.BookingResponseDto;
import tech.interview.home.assignment.supercomputer.model.dto.NewBookingRequestDto;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface BookingMapper {

    @Mappings({
            @Mapping(source = "dto.timeInterval.from", target = "timeIntervalFrom"),
            @Mapping(source = "dto.timeInterval.to", target = "timeIntervalTo")
    })
    Booking requestDtoToEntity(NewBookingRequestDto dto);

    @Mappings({
            @Mapping(source = "entity.timeIntervalFrom", target = "timeInterval.from"),
            @Mapping(source = "entity.timeIntervalTo", target = "timeInterval.to")
    })
    BookingResponseDto entityToResponseDto(Booking entity);
}
