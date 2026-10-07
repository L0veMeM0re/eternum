# Включает ANSI-цвета в текущей консоли Windows (для cmd)
$sig = @'
[DllImport("kernel32.dll", SetLastError = true)]
public static extern IntPtr GetStdHandle(int nStdHandle);
[DllImport("kernel32.dll", SetLastError = true)]
public static extern bool GetConsoleMode(IntPtr hConsoleHandle, out uint lpMode);
[DllImport("kernel32.dll", SetLastError = true)]
public static extern bool SetConsoleMode(IntPtr hConsoleHandle, uint dwMode);
'@
Add-Type -MemberDefinition $sig -Name NativeConsole -Namespace Win32 -ErrorAction SilentlyContinue | Out-Null
$h = [Win32.NativeConsole]::GetStdHandle(-11)
[uint32]$mode = 0
[void][Win32.NativeConsole]::GetConsoleMode($h, [ref]$mode)
$mode = $mode -bor 0x0004
[void][Win32.NativeConsole]::SetConsoleMode($h, $mode)
