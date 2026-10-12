$ErrorActionPreference = 'Stop'
Set-Location 'D:\github\RetroMusicPlayer'

$Apply = $args -contains '--apply'

# A /* ... */ or <!-- ... --> comment block that carries a
# "Copyright (c) <year> Hemanth Savar<ala>." line.
# - non-greedy + delimiter-tempered dot: can never run past the end of its own comment
# - prefix allows file start, line start, or right after an XML declaration's ?>
$re = [regex]::new(
    '(?s)(?:\A|(?<=\n)|(?<=\?>\s?))(/\*|<!--)(?:(?!\*/|-->).)*?Copyright \(c?C?\) \d{4} Hemanth Savar[a-z]*\.(?:(?!\*/|-->).)*?(\*/|-->)[ \t]*(?:\r?\n(?:\r?\n)?)?',
    [Text.RegularExpressions.RegexOptions]::CultureInvariant)

# Root-level IDE license template: it IS the GPL notice and nothing else.
# Orphaned (no spotless config anywhere in the build), and this repo has no commits,
# so deleting it is irreversible -> left for an explicit call, not auto-removed.
$exclude = @((Join-Path $PWD 'spotless.license.kt'))

$utf8NoBom = New-Object Text.UTF8Encoding($false)

$files = Get-ChildItem -Recurse -File -Include *.kt, *.java, *.kts, *.xml |
    Where-Object { $_.FullName -notmatch '\\build\\|\\\.git\\' -and $exclude -notcontains $_.FullName }

$matched = 0
$changed = 0
$untouched = [System.Collections.Generic.List[string]]::new()
$skippedUnsafe = [System.Collections.Generic.List[string]]::new()

foreach ($f in $files) {
    $bytes = [IO.File]::ReadAllBytes($f.FullName)
    $hasBom = $bytes.Length -ge 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF
    $raw = [IO.File]::ReadAllText($f.FullName)

    if ($raw -notmatch 'Copyright \(c?C?\) \d{4}') { continue }

    $ms = $re.Matches($raw)
    if ($ms.Count -eq 0) {
        $untouched.Add($f.FullName)
        continue
    }
    $matched += $ms.Count

    # Safety net: a match must be the GPL notice and nothing else (no code).
    $bad = @($ms | Where-Object {
        $_.Value -match 'Copyright' -and
        ($_.Value -notmatch 'GNU General Public License' -or
         $_.Value -match '\b(package|import|class|object|fun|val|var|resource)\b')
    })
    if ($bad.Count -gt 0) { $skippedUnsafe.Add($f.FullName); continue }

    if ($Apply) {
        $nl = if ($raw.Contains("`r`n")) { "`r`n" } else { "`n" }
        $out = $re.Replace($raw, '')
        # Removing an inline <?xml ...?><!-- block leaves the declaration glued to
        # the next element; put the newline back.
        if ($out -match '\A\s*<\?xml') {
            $out = [regex]::Replace($out, '^(\s*<\?xml[^>]*\?>)[ \t\r\n]*', ('$1' + $nl))
        }
        $enc = if ($hasBom) { New-Object Text.UTF8Encoding($true) } else { $utf8NoBom }
        [IO.File]::WriteAllText($f.FullName, $out, $enc)
    }
    $changed++
}

Write-Output ("APPLY={0} | blocks matched={1} | files changed={2} | files untouched={3} | skipped-unsafe={4}" -f $Apply, $matched, $changed, $untouched.Count, $skippedUnsafe.Count)
if ($untouched.Count -gt 0) {
    Write-Output '--- untouched (deliberate) ---'
    $untouched | ForEach-Object { Write-Output $_ }
}
if ($skippedUnsafe.Count -gt 0) {
    Write-Output '--- SKIPPED UNSAFE ---'
    $skippedUnsafe | ForEach-Object { Write-Output $_ }
}
