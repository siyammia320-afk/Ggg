import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

official_content = """@Composable
fun OfficialCreateTabContent(
    uiState: com.example.ui.AccountCreatorUiState,
    onPhoneChange: (String) -> Unit,
    onCountrySelected: (Country) -> Unit,
    onCreateAccount: () -> Unit,
    onCopyUid: (String) -> Unit,
    onCopyNumber: (String) -> Unit,
    onCopyCookies: (String) -> Unit,
    onOpenProxySettings: () -> Unit = {},
    onCheckLive: (String) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Compact Proxy Status Bar
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "🌐 Proxy: ${uiState.proxyStatus}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                IconButton(
                    onClick = onOpenProxySettings,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.LightGray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Main Create Account Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "⚙️ Create Account (Official)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                // Phone Input (Read-only)
                OutlinedTextField(
                    value = uiState.phoneInput,
                    onValueChange = { },
                    readOnly = true,
                    enabled = false,
                    placeholder = { Text("Select number from RANGE tab", color = Color.Gray, fontSize = 11.sp) },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = Color(0xFF2D2D2D),
                        disabledTextColor = Color.White,
                        disabledPlaceholderColor = Color.Gray
                    ),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Country Selector Dropdown
                CountryDropdownSelector(
                    selectedCountry = uiState.selectedCountry,
                    enabled = true,
                    onCountrySelected = onCountrySelected
                )

                // Password Input (Hardcoded)
                OutlinedTextField(
                    value = "arafat@@##",
                    onValueChange = {},
                    readOnly = true,
                    enabled = false,
                    leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = Color(0xFF2D2D2D),
                        disabledTextColor = Color.White,
                        disabledPlaceholderColor = Color.Gray
                    ),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // CREATE Button
                Button(
                    onClick = onCreateAccount,
                    enabled = !uiState.isCreating && uiState.phoneInput.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2563EB),
                        disabledContainerColor = Color(0xFF2D2D2D)
                    ),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                ) {
                    if (uiState.isCreating) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                            Text("CREATING...", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Text("⚡ NM OFFICAL CREATE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        // Result Card
        uiState.lastCreatedAccount?.let { account ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("✅ Account Created!", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Green)
                    Text("Number: ${account.phone}", fontSize = 11.sp, color = Color.White)
                    Text("UID: ${account.uid}", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = { onCopyUid(account.uid) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                            modifier = Modifier.weight(1f).height(32.dp)
                        ) {
                            Text("COPY UID", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Button(
                            onClick = { onCopyCookies(account.cookies) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                            modifier = Modifier.weight(1f).height(32.dp)
                        ) {
                            Text("COPY COOKIES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = { onCheckLive(account.uid) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth().height(34.dp)
                    ) {
                        Text("Live Check ⚡", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
"""

with open('app/src/main/java/com/example/MainActivity.kt', 'a') as f:
    f.write("\n" + official_content)
