# CocoR-eclipse - An Eclipse plugin for Coco/R

## What it is

This is the source for an Eclipse plugin that supports editing
attributed context-free grammars and automatically generates
parsers. It uses the parser/scanner generator Coco/R, which accepts
LL(k) grammars, to generate code in Java.

The plugin can be installed in Eclipse from the [update
site](http://ssw.jku.at/Research/Projects/Coco/Eclipse) as per the
description on the [support
page](https://ssw.jku.at/Research/Projects/Coco/Eclipse/).

## Status

I have yet to try to build this Eclipse plug-in using Eclipse PDE. The
binary version from the update site works fine, except for one issue
(if your grammar is in a subdirectory, "src" e.g., the plugin assumes
that the package where your parser and scanner fits starts with "src."
which is an easy edit but irritating since the source is re-generated
on every save of the grammar...)

## Background

Coco/R is a compiler generator created by Hanspeter Mössenböck and
Markus Löberbauer at University of Linz around 1990. You can read more
about it [here](https://ssw.jku.at/Research/Projects/Coco/).

The source code for the Coco/R proper, with various language
implementations, is available at [GitHub](https://github.com/SSW-CocoR).

As the Eclipse ecosystem supports building various tools, some of
which might need a scanner/parser, an Eclipse plugin was created in 2008
by Markus Löberbauer and Christian Wressnegger. It is hosted on
[SourceForge](https://sourceforge.net/projects/cocoeclipse/).

As [a request to move that source to
GitHub](https://github.com/SSW-CocoR/CocoR-Java/issues/1) has gone
unheeded for a while, I decided to transfer it.

## License

It is published here using the same license as the original (GPL v2),
and I take no credit for its creation, nor any blame for it ;-)

## Support

I might do some fixes if I need to, you may post issues if you want,
but expect no response.

If you can convince the SSW-gang to move it into their project, or
even give it some love, I would be very happy.
