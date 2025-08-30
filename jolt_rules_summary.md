# Jolt Transformation Rules Summary

This document lists all the generated Jolt transformation rules based on the provided specifications.

## Generated Rules

1. **rule_cpsId.json** - Transform `cpsId` parameter if not empty to `${cpsId1111}`
2. **rule_cpsWi.json** - Transform `cpsWi` parameter if not empty to `${cpsWi1111}`
3. **rule_seqNo.json** - Transform `seqNo` parameter if not empty to `${seqNo1111}`
4. **rule_orderItemReqArgs_0_itemId.json** - Transform `orderItemReqArgs[0].itemId` to `${itemId00}`
5. **rule_orderItemReqArgs_0_itemPro_dp_group.json** - Transform `orderItemReqArgs[0].itemPro.dp_group` if exists and not empty to `${dp_group00}`
6. **rule_orderItemReqArgs_0_itemPro_dp_package_code.json** - Transform `orderItemReqArgs[0].itemPro.dp_package_code` if exists and not empty to `${dp_package_code00}`
7. **rule_orderItemReqArgs_0_subOrderItemReqArgs_0_itemId.json** - Transform `orderItemReqArgs[0].subOrderItemReqArgs[0].itemId` if exists and not empty to `${itemId0000}`
8. **rule_orderItemReqArgs_0_subOrderItemReqArgs_0_itemPro_dp_group.json** - Transform `orderItemReqArgs[0].subOrderItemReqArgs[0].itemPro.dp_group` if exists and not empty to `${dp_group0000}`
9. **rule_orderItemReqArgs_0_subOrderItemReqArgs_0_itemPro_dp_package_code.json** - Transform `orderItemReqArgs[0].subOrderItemReqArgs[0].itemPro.dp_package_code` if exists and not empty to `${dp_package_code0000}`
10. **rule_orderItemReqArgs_0_subOrderItemReqArgs_1_itemId.json** - Transform `orderItemReqArgs[0].subOrderItemReqArgs[1].itemId` if exists and not empty to `${itemId0001}`
11. **rule_orderItemReqArgs_0_subOrderItemReqArgs_1_itemPro_dp_group.json** - Transform `orderItemReqArgs[0].subOrderItemReqArgs[1].itemPro.dp_group` if exists and not empty to `${dp_group0001}`
12. **rule_orderItemReqArgs_0_subOrderItemReqArgs_1_itemPro_dp_package_code.json** - Transform `orderItemReqArgs[0].subOrderItemReqArgs[1].itemPro.dp_package_code` if exists and not empty to `${dp_package_code0001}`
13. **rule_orderItemReqArgs_1_itemId.json** - Transform `orderItemReqArgs[1].itemId` to `${itemId01}`
14. **rule_orderItemReqArgs_1_itemPro_dp_group.json** - Transform `orderItemReqArgs[1].itemPro.dp_group` if exists and not empty to `${dp_group01}`
15. **rule_orderItemReqArgs_1_itemPro_dp_package_code.json** - Transform `orderItemReqArgs[1].itemPro.dp_package_code` if exists and not empty to `${dp_package_code01}`
16. **rule_orderItemReqArgs_1_subOrderItemReqArgs_0_itemId.json** - Transform `orderItemReqArgs[1].subOrderItemReqArgs[0].itemId` if exists and not empty to `${itemId0100}`
17. **rule_orderItemReqArgs_1_subOrderItemReqArgs_0_itemPro_dp_group.json** - Transform `orderItemReqArgs[1].subOrderItemReqArgs[0].itemPro.dp_group` if exists and not empty to `${dp_group0100}`
18. **rule_orderItemReqArgs_1_subOrderItemReqArgs_0_itemPro_dp_package_code.json** - Transform `orderItemReqArgs[1].subOrderItemReqArgs[0].itemPro.dp_package_code` if exists and not empty to `${dp_package_code0100}`
19. **rule_couponList_0_couponCodes.json** - Transform `couponList[0].couponCodes` if not empty to `${couponCodes0000}`
20. **rule_orderSouce.json** - Transform `orderSouce` to `${orderSouce00}`
21. **rule_salePortal.json** - Transform `salePortal` to `${salePortal00}`
22. **rule_carrierInvoiceVOs_0_carrierCode.json** - Transform `carrierInvoiceVOs[0].carrierCode` if not empty and not equal to "VMALL-HUAWEIDEVICE" to `${carrierCode0000}`

## Rule Types

### Conditional Rules (with null/empty checks)
- Rules that only replace values if the parameter exists and is not empty
- Use Jolt's conditional logic with `if()` function

### Unconditional Rules
- Rules that always replace the parameter value
- Direct value assignment

### Special Conditional Rules
- `carrierInvoiceVOs[0].carrierCode` has additional condition to exclude "VMALL-HUAWEIDEVICE" value

## Usage
Each rule file can be used independently or combined as needed for your Jolt transformation pipeline.