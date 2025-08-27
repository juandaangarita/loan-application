package com.onix.api.mapper;

import com.onix.api.dto.CreateLoanDTO;
import com.onix.api.dto.LoanDTO;
import com.onix.model.loanapplication.Loan;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LoanMapper {
    @Mapping(target = "loanStatus", ignore = true)
    @Mapping(target = "loanType", ignore = true)
    LoanDTO toDto(Loan loan);

    @Mapping(target = "loanId", ignore = true)
    Loan toModel(CreateLoanDTO dto);
}
