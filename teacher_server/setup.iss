; Inno Setup 安装脚本
; 用于创建 Windows 安装程序

[Setup]
AppName=茶文化课程教师端服务器
AppVersion=1.0
AppPublisher=您的公司名称
AppPublisherURL=
AppSupportURL=
AppUpdatesURL=
DefaultDirName={pf}\TeacherServer
DefaultGroupName=茶文化课程
AllowNoIcons=yes
LicenseFile=
OutputDir=installer
OutputBaseFilename=TeacherServer_Setup
SetupIconFile=
Compression=lzma
SolidCompression=yes
ArchitecturesInstallIn64BitMode=x64
PrivilegesRequired=admin

[Languages]
Name: "chinesesimp"; MessagesFile: "compiler:Languages\ChineseSimplified.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked
Name: "quicklaunchicon"; Description: "{cm:CreateQuickLaunchIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked; OnlyBelowVersion: 6.1

[Files]
Source: "dist\TeacherServer.exe"; DestDir: "{app}"; Flags: ignoreversion
Source: "config.py"; DestDir: "{app}"; Flags: ignoreversion
Source: "init_db.py"; DestDir: "{app}"; Flags: ignoreversion
Source: "启动服务器.bat"; DestDir: "{app}"; Flags: ignoreversion
Source: "初始化数据库.bat"; DestDir: "{app}"; Flags: ignoreversion
Source: "安装说明.txt"; DestDir: "{app}"; Flags: ignoreversion
; 注意：需要根据实际打包结果调整文件路径

[Icons]
Name: "{group}\教师端服务器"; Filename: "{app}\TeacherServer.exe"
Name: "{group}\启动服务器"; Filename: "{app}\启动服务器.bat"
Name: "{group}\初始化数据库"; Filename: "{app}\初始化数据库.bat"
Name: "{group}\{cm:UninstallProgram,茶文化课程教师端服务器}"; Filename: "{uninstallexe}"
Name: "{autodesktop}\教师端服务器"; Filename: "{app}\TeacherServer.exe"; Tasks: desktopicon
Name: "{userappdata}\Microsoft\Internet Explorer\Quick Launch\教师端服务器"; Filename: "{app}\TeacherServer.exe"; Tasks: quicklaunchicon

[Run]
Filename: "{app}\初始化数据库.bat"; Description: "初始化数据库"; Flags: nowait postinstall skipifsilent
Filename: "{app}\TeacherServer.exe"; Description: "启动服务器"; Flags: nowait postinstall skipifsilent

[Code]
procedure InitializeWizard;
begin
  // 可以在这里添加自定义初始化代码
end;

