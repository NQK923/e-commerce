package com.learnfirebase.ecommerce.report.application.port.in;

import com.learnfirebase.ecommerce.common.application.UseCase;
import com.learnfirebase.ecommerce.report.application.command.GenerateReportCommand;
import com.learnfirebase.ecommerce.report.application.dto.ReportDto;

public interface GenerateDailyReportUseCase extends UseCase {
    ReportDto execute(GenerateReportCommand command);
}
