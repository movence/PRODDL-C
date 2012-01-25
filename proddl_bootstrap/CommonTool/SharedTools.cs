/*
 * 
 * Copyright J. Craig Venter Institute, 2011
 * 
 * The creation of this program was supported by the U.S. National
 * Science Foundation grant 1048199 and the Microsoft allocation
 * in the MS Azure cloud.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Diagnostics;
using System.IO;

using SevenZip;

namespace CommonTool
{
    public class SharedTools
    {
        public Process buildCloudProcess(String fileName, String args, String outputTag)
        {
            Process proc = new Process();
            proc.StartInfo.RedirectStandardOutput = true;
            proc.StartInfo.RedirectStandardError = true;
            proc.StartInfo.ErrorDialog = false;
            proc.StartInfo.CreateNoWindow = true;
            proc.StartInfo.UseShellExecute = false;
            proc.EnableRaisingEvents = false;
            proc.ErrorDataReceived += (sender, e) => { if (e.Data != null) Trace.WriteLine(outputTag + ":ERROR>> " + e.Data); };
            proc.OutputDataReceived += (sender, e) => { if (e.Data != null) Trace.WriteLine(outputTag + ":OUTPUT>> " + e.Data); };

            proc.StartInfo.FileName = fileName;
            proc.StartInfo.Arguments = args;

            return proc;
        }

        public Boolean extractZipFile(String filePath, String extractTo)
        {
            try
            {
                string sevenZipPath = Path.Combine(Directory.GetCurrentDirectory(), @"tools\7z64.dll");
                SevenZipExtractor.SetLibraryPath(sevenZipPath);
                SevenZipExtractor extractor = new SevenZipExtractor(filePath);
                extractor.ExtractArchive(extractTo);
                return true;
            }
            catch (Exception ex)
            {
                Trace.TraceError("EXCEPTION: extractZipFile() - " + ex.ToString());
                return false;
            }
        }
    }
}
