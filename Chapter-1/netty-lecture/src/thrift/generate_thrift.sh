#!/usr/bin/env sh

## thrift --gen <language> <Thrift filename>
## 生成Java thrift
thrift --gen java src/thrift/data.thrift
thrift --gen py src/thrift/data.thrift
