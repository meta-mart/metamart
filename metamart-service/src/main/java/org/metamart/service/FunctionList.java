package org.metamart.service;

import java.util.List;
import org.metamart.schema.type.Function;
import org.metamart.service.util.ResultList;

public class FunctionList extends ResultList<Function> {
  @SuppressWarnings("unused")
  public FunctionList() {}

  public FunctionList(List<Function> data) {
    super(data, null, null, data.size());
  }
}
