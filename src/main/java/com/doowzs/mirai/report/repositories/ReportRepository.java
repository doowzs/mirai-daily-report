package com.doowzs.mirai.report.repositories;

import com.doowzs.mirai.report.models.Report;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ReportRepository extends MongoRepository<Report, String> {

}
