import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# Replace TabRow with ScrollableTabRow having edgePadding = 0.dp
# Actually user explicitly wants them all visible on one screen. 
# So TabRow is best but with 6 tabs, let's just make the fonts very small (9.sp) and horizontal padding very small (1.dp).

tab_row_pattern = r"TabRow\(\s*selectedTabIndex = selectedTabIndex,\s*containerColor = Color\(0xFFF1F5F9\),\s*contentColor = Color\(0xFF1E293B\),\s*indicator = \{ tabPositions ->\s*TabRowDefaults\.SecondaryIndicator\(\s*Modifier\.tabIndicatorOffset\(tabPositions\[selectedTabIndex\]\),\s*color = Color\(0xFF2563EB\),\s*height = 3\.dp\s*\)\s*\}\s*\) \{"
tab_row_replacement = """TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color(0xFFF1F5F9),
                    contentColor = Color(0xFF1E293B),
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = Color(0xFF2563EB),
                            height = 3.dp
                        )
                    }
                ) {"""

# Replace Tab 0
content = re.sub(r'modifier = Modifier\.testTag\("tab_get_number"\)\s*\) \{\s*Row\([^)]*\)\s*\{\s*Icon\(\s*Icons\.Default\.Smartphone[^)]*\)\s*Text\(\s*text = "NUMBER"[^)]*\)\s*\}', 
r'''modifier = Modifier.testTag("tab_get_number")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 0.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "RANGE",
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 8.sp,
                                color = if (selectedTabIndex == 0) Color(0xFF1E293B) else Color(0xFF64748B),
                                maxLines = 1
                            )
                        }''', content, count=1)

# Replace Tab 1
content = re.sub(r'modifier = Modifier\.testTag\("tab_create"\)\s*\) \{\s*Row\([^)]*\)\s*\{\s*Icon\(\s*Icons\.Default\.PersonAdd[^)]*\)\s*Text\(\s*text = "NM CREATE"[^)]*\)\s*\}', 
r'''modifier = Modifier.testTag("tab_create")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 0.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "NM LIMIT",
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 8.sp,
                                color = if (selectedTabIndex == 1) Color(0xFF1E293B) else Color(0xFF64748B),
                                maxLines = 1
                            )
                        }''', content, count=1)

# Replace Tab 2
content = re.sub(r'modifier = Modifier\.testTag\("tab_email_create"\)\s*\) \{\s*Row\([^)]*\)\s*\{\s*Icon\(\s*Icons\.Default\.Email[^)]*\)\s*Text\(\s*text = "EMAIL CREATE"[^)]*\)\s*\}', 
r'''modifier = Modifier.testTag("tab_official")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 0.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "NM OFFICAL",
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 8.sp,
                                color = if (selectedTabIndex == 2) Color(0xFF1E293B) else Color(0xFF64748B),
                                maxLines = 1
                            )
                        }
                    }
                    Tab(
                        selected = selectedTabIndex == 3,
                        onClick = { selectedTabIndex = 3 },
                        modifier = Modifier.testTag("tab_email_create")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 0.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "EML LIMIT",
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 8.sp,
                                color = if (selectedTabIndex == 3) Color(0xFF1E293B) else Color(0xFF64748B),
                                maxLines = 1
                            )
                        }''', content, count=1)

# Replace Tab 3
content = re.sub(r'modifier = Modifier\.testTag\("tab_inbox"\)\s*\) \{\s*Row\([^)]*\)\s*\{\s*Icon\(\s*Icons\.Default\.Sms[^)]*\)\s*Text\(\s*text = "INBOX[^"]*"[^)]*\)\s*\}', 
r'''modifier = Modifier.testTag("tab_inbox")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 0.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "OTP",
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 8.sp,
                                color = if (selectedTabIndex == 4) Color(0xFF1E293B) else Color(0xFF64748B),
                                maxLines = 1
                            )
                        }''', content, count=1)

# Replace Tab 4
content = re.sub(r'modifier = Modifier\.testTag\("tab_history"\)\s*\) \{\s*Row\([^)]*\)\s*\{\s*Icon\(\s*Icons\.Default\.History[^)]*\)\s*Text\(\s*text = "SAVED[^"]*"[^)]*\)\s*\}', 
r'''modifier = Modifier.testTag("tab_history")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 0.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "SAV",
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 8.sp,
                                color = if (selectedTabIndex == 5) Color(0xFF1E293B) else Color(0xFF64748B),
                                maxLines = 1
                            )
                        }''', content, count=1)

# Replace the 'when'
content = content.replace("when (selectedTabIndex) {\n                    0 -> GetNumberTabContent(", """when (selectedTabIndex) {
                    0 -> GetNumberTabContent(""")
content = content.replace("1 -> CreateAccountTabContent(", "1 -> CreateAccountTabContent(")
content = content.replace("2 -> EmailCreateTabContent(", """2 -> OfficialCreateTabContent(
                        uiState = uiState,
                        onPhoneChange = viewModel::onPhoneChanged,
                        onCountrySelected = viewModel::onCountrySelected,
                        onCreateAccount = { viewModel.createAccountOfficial(context) },
                        onCopyUid = { uid -> viewModel.copyToClipboard(context, uid, "UID") },
                        onCopyNumber = { num -> viewModel.copyToClipboard(context, num, "NUMBER") },
                        onCopyCookies = { cookie -> viewModel.copyToClipboard(context, cookie, "COOKIES") },
                        onOpenProxySettings = viewModel::openProxyDialog,
                        onCheckLive = viewModel::checkLiveStatusForSingleAccount
                    )
                    3 -> EmailCreateTabContent(""")
content = content.replace("3 -> InboxTabContent(", "4 -> InboxTabContent(")
content = content.replace("4 -> AccountHistoryTabContent(", "5 -> AccountHistoryTabContent(")


# Replace "selectedTabIndex == 3" with "selectedTabIndex == 4" inside tab 3 icon tint and text color? Wait, I completely removed the Icons in my regex substitutions! Let's check the regex again, I replaced the whole `Row(...) { Icon(...) Text(...) }` with just `Row(...) { Text(...) }`. So I don't need to worry about Icon tint. 

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
