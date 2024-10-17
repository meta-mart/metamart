/*
 *  Copyright 2021 DigiTrans
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.metamart.service.jdbi3;

import lombok.extern.slf4j.Slf4j;
import org.metamart.schema.entity.data.Report;
import org.metamart.schema.type.EntityReference;
import org.metamart.service.Entity;
import org.metamart.service.resources.reports.ReportResource;
import org.metamart.service.util.EntityUtil;
import org.metamart.service.util.EntityUtil.Fields;

@Slf4j
public class ReportRepository extends EntityRepository<Report> {
  public ReportRepository() {
    super(
        ReportResource.COLLECTION_PATH,
        Entity.REPORT,
        Report.class,
        Entity.getCollectionDAO().reportDAO(),
        "",
        "");
  }

  @Override
  public void setFields(Report report, Fields fields) {
    report.setService(getService(report)); // service is a default field
    if (report.getUsageSummary() == null) {
      report.withUsageSummary(
          fields.contains("usageSummary")
              ? EntityUtil.getLatestUsage(daoCollection.usageDAO(), report.getId())
              : report.getUsageSummary());
    }
  }

  @Override
  public void clearFields(Report report, Fields fields) {
    report.withUsageSummary(fields.contains("usageSummary") ? report.getUsageSummary() : null);
  }

  @Override
  public void prepare(Report report, boolean update) {
    // TODO report does not have service yet
  }

  @Override
  public void storeEntity(Report report, boolean update) {
    store(report, update);
  }

  @Override
  public void storeRelationships(Report report) {
    addServiceRelationship(report, report.getService());
  }

  private EntityReference getService(Report report) {
    return null;
  }
}
