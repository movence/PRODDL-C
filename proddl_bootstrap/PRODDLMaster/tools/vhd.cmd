@echo off
echo create vdisk file=C:\master.vhd maximum=200 type=fixed > "%TEMP%\script.txt"
echo select vdisk file=C:\master.vhd >> "%TEMP%\script.txt"
echo attach vdisk >> "%TEMP%\script.txt"
echo create partition primary >> "%TEMP%\script.txt"
echo format fs=ntfs label=vhd quick >> "%TEMP%\script.txt"
echo assign letter=v >> "%TEMP%\script.txt"
echo detach vdisk >> "%TEMP%\script.txt"
diskpart -s %TEMP%\script.txt
goto exit
:exit

