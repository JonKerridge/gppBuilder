package gppBuilder

class GPPlexingMethods  {

    String error = ""

    String preNetwork = "\n"
    String network = "\n"
    String postNetwork = "\n"

    String logChanAdd = "    visLogChan : logChan.out(),\n"

    def processNames = []
    def chanNumber = 1
    String currentOutChanName = "chan$chanNumber"
    String currentInChanName = "chan$chanNumber"
    ChanTypeEnum expectedInChan
    String chanSize

    List <String> inText = []
    List <String> outText = []
    int currentLine = 1
    int endLine = 0

    boolean pattern = false
    boolean logging = false

    //SH added: need logFileName gppVis command readLog()
    String logFileName;

    def getInput(FileReader reader) {
        reader.each{String line ->
            if (line.size() ==  0) line = " "
            else line = line.trim()
            inText << line
        }
        // copy package line to outText
        outText << inText[0] + "\n\n"
        outText << "import jcsp.lang.*\nimport groovyJCSP.*\n"
        reader.close()
    }

    def putOutput(FileWriter writer) {
        // now copy the source strings to outText
        outText << preNetwork
        outText << network
        outText << postNetwork
        // now copy outText to the output file
        outText.each { line ->
            writer.write(line)
        }
        println "Transformation Completed $error"
        writer.flush()
        writer.close()
    }

    def swapChannelNames = { ChanTypeEnum expected ->
        currentInChanName = currentOutChanName
        chanNumber += 1
        currentOutChanName = "chan$chanNumber"
        expectedInChan = expected
    }

    def confirmChannel = { String pName, ChanTypeEnum actualInChanType  ->
        if (expectedInChan != actualInChanType) {
            network += "Expected a process with a *$expectedInChan* type input; found $pName with type $actualInChanType \n"
            error += " with errors, see the parsed output file"
        }
    }

    def nextProcSpan = { start ->
        int beginning = start
        while (! (inText[beginning] =~ /new/)) beginning++
        int ending = beginning
        while (! inText[ending].endsWith(")")) ending++
        return [beginning, ending]
    }

    def scanChanSize = { List l ->
        int line
        for ( i in (int)l[0]..(int)l[1]){
            if ((inText[i] =~ /workers/) ||
                    (inText[i] =~ /mappers/) ||
                    (inText[i] =~ /reducers/) ||
                    (inText[i] =~ /groups/)   ) {
                line = i
                break
            }
        }
        // we now know we have found the right line
        int colon = inText[line].indexOf(":")+1
        int end = inText[line].indexOf(",")
        if (end == -1) end = inText[line].indexOf(")")
//		println "$line, ${inText[line]}, $colon, $end"
        if (end != -1) {
            chanSize = inText[line].subSequence(colon, end).trim()
            return chanSize
        }
        else return null
    }

    // closure to find a process def assuming start is the index of a line containing such a def
    def findProcDef = {int start ->
        int ending = start
        while (! inText[ending].endsWith(")")) ending++
        int startIndex = inText[start].indexOf("new") + 4
        int endIndex =  inText[start].indexOf("(")
        if (startIndex == -1 || endIndex == -1)  {
            error += "string *new* found in an unexpected place\n${inText[currentLine]}\n"
            network += error
            return null
        }
        else {
            String processName = inText[start].subSequence(startIndex, endIndex).trim()
            startIndex = inText[start].indexOf("def") + 4
            endIndex =  inText[start].indexOf("=")
            String procName = inText[start].subSequence(startIndex, endIndex)
            return [ending, processName, procName]
        }
    }

    def findNextProc = {
        currentLine = endLine + 1
        while (! (inText[currentLine] =~ /new/)) {
            network += inText[currentLine] + "\n" // add blank and comment lines
            currentLine++
        }

    }

    def extractProcDefParts = {int line ->
        int len = inText[line].size()
        int openParen = inText[line].indexOf("(")
        int closeParen = inText[line].indexOf(")")	// could be -1
        String initialDef = inText[line].subSequence(0, openParen+1) // includes the (
        String remLine = null
        String firstProperty = null
        if (closeParen > 0)	{
            // single line definition
            remLine = inText[line].subSequence(openParen+1, closeParen+1).trim()
        }
        else {
            //multi line definition
            if (openParen == (len-1)) firstProperty = " " // no property specified
            else firstProperty = inText[line].subSequence(openParen+1, len).trim()
        }
        return [initialDef, remLine, firstProperty ]
    }

    def copyProcProperties = {List rvs, int starting, int ending ->
        if (rvs[2] == null) network += "    ${rvs[1]}\n"
        else {
            if (rvs[2] != " ") network += "    ${rvs[2]}\n"
            for ( i in starting+1 .. ending) network +=  "    " +inText[i] +"\n"
        }
    }

    def checkNoProperties = {List rvs ->
        if (rvs[1] != ")") {
            error += "expecting a closing ) on same line; but not found\n"
            network += error
        }
    }

    //
    // define the closures for each process type in the library
    //
// cluster connectors
    def NodeRequestingFanAny = { String processName, int starting, int ending ->
        println "$processName: $starting, $ending"
        network += inText[starting]
    }

    def NodeRequestingFanList = { String processName, int starting, int ending ->
        println "$processName: $starting, $ending"
        network += inText[starting]
    }

    def NodeRequestingParCastList = { String processName, int starting, int ending ->
        println "$processName: $starting, $ending"
        network += inText[starting]
    }

    def NodeRequestingSeqCastAny = { String processName, int starting, int ending ->
        println "$processName: $starting, $ending"
    }

    def NodeRequestingSeqCastList = { String processName, int starting, int ending ->
        println "$processName: $starting, $ending"
        network += inText[starting]
    }

    def OneNodeRequestedList = { String processName, int starting, int ending ->
        println "$processName: $starting, $ending"
        network += inText[starting]
    }

// reducers
    def AnyFanOne = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
        confirmChannel(processName, ChanTypeEnum.any)
        def rvs = extractProcDefParts(starting)
        network += rvs[0] + "\n"
        network += "    inputAny: ${currentInChanName}.in(),\n"
        network += "    output: ${currentOutChanName}.out(),\n"
        copyProcProperties(rvs, starting, ending)
        preNetwork = preNetwork + "def $currentOutChanName = Channel.one2one()\n"
        swapChannelNames(ChanTypeEnum.one)

        //SH added
        if (logging){
            network += "\n    //gppVis command\n";
            network += "    Visualiser.hb.getChildren().add(new Connector(Connector.TYPE.REDUCER));\n";
        }
    }

    def ListFanOne = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
        confirmChannel(processName, ChanTypeEnum.list)
        def rvs = extractProcDefParts(starting)
        network += rvs[0] + "\n"
        network += "    inputList: ${currentInChanName}InList,\n"
        network += "    output: ${currentOutChanName}.out(),\n"
        checkNoProperties(rvs)
        copyProcProperties(rvs, starting, ending)
        preNetwork = preNetwork + "def $currentOutChanName = Channel.one2one()\n"
        swapChannelNames(ChanTypeEnum.one)
    }

    def ListMergeOne = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
        confirmChannel(processName, ChanTypeEnum.list)
        def rvs = extractProcDefParts(starting)
        network += rvs[0] + "\n"
        network += "    inputList: ${currentInChanName}InList,\n"
        network += "    output: ${currentOutChanName}.out(),\n"
        checkNoProperties(rvs)
        copyProcProperties(rvs, starting, ending)
        preNetwork = preNetwork + "def $currentOutChanName = Channel.one2one()\n"
        swapChannelNames(ChanTypeEnum.one)

        //SH added
        if (logging){
            network += "\n    //gppVis command\n";
            network += "    Visualiser.hb.getChildren().add(new Connector(Connector.TYPE.REDUCER));\n";
        }
    }

    def ListParOne = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
        ListFanOne(processName, starting, ending)
    }

    def ListSeqOne = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
        ListFanOne(processName, starting, ending)
    }

    def N_WayMerge = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
        confirmChannel(processName, ChanTypeEnum.list)
        def rvs = extractProcDefParts(starting)
        network += rvs[0] + "\n"
        network += "    inputList: ${currentInChanName}InList,\n"
        network += "    output: ${currentOutChanName}.out(),\n"
        copyProcProperties(rvs, starting, ending)
        preNetwork = preNetwork + "def $currentOutChanName = Channel.one2one()\n"
        swapChannelNames(ChanTypeEnum.one)

    }


// spreaders
    def AnyFanAny = { String processName, int starting, int ending ->
        println "$processName: $starting, $ending"
        confirmChannel(processName, ChanTypeEnum.any)
        def rvs = extractProcDefParts(starting)
        network += rvs[0] + "\n"
        network += "    input: ${currentInChanName}.in(),\n"
        network += "    outputAny: ${currentOutChanName}.out(),\n"
        copyProcProperties(rvs, starting, ending)
        preNetwork = preNetwork + "def $currentOutChanName = Channel.one2any()\n"
        swapChannelNames(ChanTypeEnum.any)
    }

    def AnySeqCastAny = { String processName, int starting, int ending ->
        println "$processName: $starting, $ending"
        confirmChannel(processName, ChanTypeEnum.any)
        def rvs = extractProcDefParts(starting)
        network += rvs[0] + "\n"
        network += "    input: ${currentInChanName}.in(),\n"
        network += "    outputAny: ${currentOutChanName}.out(),\n"
        copyProcProperties(rvs, starting, ending)
        preNetwork = preNetwork + "def $currentOutChanName = Channel.one2any()\n"
        swapChannelNames(ChanTypeEnum.any)
    }

    def OneDirectedList = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
        confirmChannel(processName, ChanTypeEnum.one)
        def rvs = extractProcDefParts(starting)
        network += rvs[0] + "\n"
        network += "    input: ${currentInChanName}.in(),\n"
        network += "    outputList: ${currentOutChanName}OutList,\n"
        copyProcProperties(rvs, starting, ending)
        rvs = nextProcSpan(ending + 2)
        String returnedChanSize = scanChanSize(rvs)
        if (returnedChanSize != null) chanSize = returnedChanSize
        preNetwork = preNetwork + "def $currentOutChanName = Channel.one2oneArray($chanSize)\n"
        preNetwork = preNetwork + "def ${currentOutChanName}OutList = new ChannelOutputList($currentOutChanName)\n"
        preNetwork = preNetwork + "def ${currentOutChanName}InList = new ChannelInputList($currentOutChanName)\n"
        swapChannelNames(ChanTypeEnum.list)
    }

    def OneFanAny = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
        confirmChannel(processName, ChanTypeEnum.one)
        def rvs = extractProcDefParts(starting)
        network += rvs[0] + "\n"
        network += "    input: ${currentInChanName}.in(),\n"
        network += "    outputAny: ${currentOutChanName}.out(),\n"
        copyProcProperties(rvs, starting, ending)
        preNetwork = preNetwork + "def $currentOutChanName = Channel.one2any()\n"
        swapChannelNames(ChanTypeEnum.any)

        //SH added
        if (logging){
            network += "\n    //gppVis command\n";
            network += "    Visualiser.hb.getChildren().add(new Connector(Connector.TYPE.SPREADER));\n";
        }
    }

    def OneFanList = { String processName, int starting, int ending ->
//			println "$processName: $starting, $ending"
        confirmChannel(processName, ChanTypeEnum.one)
        def rvs = extractProcDefParts(starting)
        network += rvs[0] + "\n"
        network += "    input: ${currentInChanName}.in(),\n"
        network += "    outputList: ${currentOutChanName}OutList )\n"
        checkNoProperties(rvs)
        rvs = nextProcSpan(ending + 2)
        String returnedChanSize = scanChanSize(rvs)
        if (returnedChanSize != null) chanSize = returnedChanSize
        preNetwork = preNetwork + "def $currentOutChanName = Channel.one2oneArray($chanSize)\n"
        preNetwork = preNetwork + "def ${currentOutChanName}OutList = new ChannelOutputList($currentOutChanName)\n"
        preNetwork = preNetwork + "def ${currentOutChanName}InList = new ChannelInputList($currentOutChanName)\n"
        swapChannelNames(ChanTypeEnum.list)

        //SH added
        if (logging){
            network += "\n    //gppVis command\n";
            network += "    Visualiser.hb.getChildren().add(new Connector(Connector.TYPE.SPREADER));\n";
        }
    }

    def OneFanRequestedAny = { String processName, int starting, int ending ->
        println "$processName: $starting, $ending"
        network += inText[starting]

    }

    def OneIndexedList = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
        OneDirectedList(processName, starting, ending)
    }

    def OneParCastList = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
        OneFanList(processName, starting, ending)
    }

    def OneSeqCastAny = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
        OneFanAny(processName, starting, ending)
    }

    def OneSeqCastList = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
        OneFanList(processName, starting, ending)
    }

    def RequestingFanAny = { String processName, int starting, int ending ->
        println "$processName: $starting, $ending"
        network += inText[starting]

    }

    def RequestingFanList = { String processName, int starting, int ending ->
        println "$processName: $starting, $ending"
        network += inText[starting]

    }

    def RequestingParCastList = { String processName, int starting, int ending ->
        println "$processName: $starting, $ending"
        network += inText[starting]

    }

    def RequestingSeqCastAny = { String processName, int starting, int ending ->
        println "$processName: $starting, $ending"
        network += inText[starting]

    }

    def RequestingSeqCastList = { String processName, int starting, int ending ->
        println "$processName: $starting, $ending"
        network += inText[starting]
    }

// divide and conquer
    def BasicDandC = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
        confirmChannel(processName, ChanTypeEnum.one)
        def rvs = extractProcDefParts(starting)
        network += rvs[0] + "\n"
        network += "    input: ${currentInChanName}.in(),\n"
        network += "    output: ${currentOutChanName}.out(),\n"
        copyProcProperties(rvs, starting, ending)
        preNetwork = preNetwork + "def $currentOutChanName = Channel.one2one()\n"
        swapChannelNames(ChanTypeEnum.one)
    }

    def Node = { String processName, int starting, int ending ->
        println "$processName: $starting, $ending"
        network += inText[starting]
    }

    def Root = { String processName, int starting, int ending ->
        println "$processName: $starting, $ending"
    }

// mapReduce
    def OneMapperMany = { String processName, int starting, int ending ->
        println "$processName: $starting, $ending"
        network += inText[starting]
    }

    def OneMapperOne = { String processName, int starting, int ending ->
        println "$processName: $starting, $ending"
        network += inText[starting]
    }

    def Reducer = { String processName, int starting, int ending ->
//			println "$processName: $starting, $ending"
        confirmChannel(processName, ChanTypeEnum.list)
        def rvs = extractProcDefParts(starting)
        network += rvs[0] + "\n"
        network += "    inputList: ${currentInChanName}InList,\n"
        network += "    output: ${currentOutChanName}.out(),\n"
        copyProcProperties(rvs, starting, ending)
        // no need to scan as this process only has a single output
        preNetwork = preNetwork + "def $currentOutChanName = Channel.one2one()\n"
        swapChannelNames(ChanTypeEnum.one)
    }

// patterns
    def DataParallelCollect = { String processName, int starting, int ending ->
//			println "$processName: $starting, $ending"
        pattern = true
        def rvs = extractProcDefParts(starting)
        network += rvs[0] + "\n"
        copyProcProperties(rvs, starting, ending)
    }

    def TaskParallelCollect = { String processName, int starting, int ending ->
//			println "$processName: $starting, $ending"
        pattern = true
        def rvs = extractProcDefParts(starting)
        network += rvs[0] + "\n"
        copyProcProperties(rvs, starting, ending)
    }

    def TaskParallelOfGroupCollects = { String processName, int starting, int ending ->
//			println "$processName: $starting, $ending"
        pattern = true
        def rvs = extractProcDefParts(starting)
        network += rvs[0] + "\n"
        copyProcProperties(rvs, starting, ending)
    }

// evolutionary

    def ParallelClientServerEngine = { String processName, int starting, int ending ->
//          println "$processName: $starting, $ending"
        pattern = true
        def rvs = extractProcDefParts(starting)
        network += rvs[0] + "\n"
        copyProcProperties(rvs, starting, ending)
    }

// composites
    def GroupOfPipelineCollects = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
        confirmChannel(processName, ChanTypeEnum.any)
        def rvs = extractProcDefParts(starting)
        network += rvs[0] + "\n"
        network += "    inputAny: ${currentInChanName}.in(),\n"
        if (logging) network += logChanAdd
        network += "    // no output channel required\n"
        copyProcProperties(rvs, starting, ending)

        //SH added
        if (logging){
            network += "\n    //gppVis command\n";
            String lineA = inText[starting+3] //contains variable for number of groups
            String[] wordsA = lineA.split(" ")
            String lineB = inText[starting+4] //contains logPhaseNames
            String[] wordsB = lineB.split("\\[|\\]")
//			network += wordsB
            network += "    Visualiser.hb.getChildren().add(Visualiser.p.addGoP(" +wordsA[1]+ " " +  wordsB[1]+ "));\n";
        }
    }

    def GroupOfPipelines = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
        confirmChannel(processName, ChanTypeEnum.list)
        def rvs = extractProcDefParts(starting)
        network += rvs[0] + "\n"
        network += "    inputList: ${currentInChanName}InList,\n"
        network += "    outputList: ${currentOutChanName}OutList,\n"
        copyProcProperties(rvs, starting, ending)
        preNetwork = preNetwork + "def $currentOutChanName = Channel.one2oneArray($chanSize)\n"
        preNetwork = preNetwork + "def ${currentOutChanName}OutList = new ChannelOutputList($currentOutChanName)\n"
        preNetwork = preNetwork + "def ${currentOutChanName}InList = new ChannelInputList($currentOutChanName)\n"
        swapChannelNames(ChanTypeEnum.list)
    }

    def PipelineOfGroupCollects = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
        GroupOfPipelineCollects	(processName, starting , ending)
    }

    def PipelineOfGroups = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
        AnyGroupAny(processName, starting, ending)
    }

// groups
    def AnyGroupAny = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
        confirmChannel(processName, ChanTypeEnum.any)
        def rvs = extractProcDefParts(starting)
        network += rvs[0] + "\n"
        network += "    inputAny: ${currentInChanName}.in(),\n"
        network += "    outputAny: ${currentOutChanName}.out(),\n"
        copyProcProperties(rvs, starting, ending)
        rvs = nextProcSpan(ending + 2)
        preNetwork = preNetwork + "def $currentOutChanName = Channel.any2any()\n"
        swapChannelNames(ChanTypeEnum.any)

        //SH added
        if (logging){
            network += "\n    //gppVis command\n";
            String lineA = inText[starting] //contains variable for number of workers
            String[] wordsA = lineA.split(":")
            String lineB = inText[starting+2] //contains logPhaseName
            String[] wordsB = lineB.split("\"")
            network += "    Visualiser.hb.getChildren().add(Visualiser.p.addWorkers(" +wordsA[1]+  " \""+wordsB[1]+"\"));\n";
        }
    }

    def AnyGroupCollect = { String processName, int starting, int ending ->
//      println "$processName: $starting, $ending"
        confirmChannel(processName, ChanTypeEnum.any)
        def rvs = extractProcDefParts(starting)
        network += rvs[0] + "\n"
        network += "    inputAny: ${currentInChanName}.in(),\n"
        if (logging) network += logChanAdd
        network += "    // no output channel required\n"
        copyProcProperties(rvs, starting, ending)
    }

    def AnyGroupList = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
        confirmChannel(processName, ChanTypeEnum.any)
        def rvs = extractProcDefParts(starting)
        network += rvs[0] + "\n"
        network += "    inputAny: ${currentInChanName}.in(),\n"
        network += "    outputList: ${currentOutChanName}OutList,\n"
        copyProcProperties(rvs, starting, ending)
        rvs = nextProcSpan(ending + 2)
        String returnedChanSize = scanChanSize(rvs)
        if (returnedChanSize != null) chanSize = returnedChanSize
        preNetwork = preNetwork + "def $currentOutChanName = Channel.one2oneArray($chanSize)\n"
        preNetwork = preNetwork + "def ${currentOutChanName}OutList = new ChannelOutputList($currentOutChanName)\n"
        preNetwork = preNetwork + "def ${currentOutChanName}InList = new ChannelInputList($currentOutChanName)\n"
        swapChannelNames(ChanTypeEnum.list)
    }

    def ListGroupAny = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
        confirmChannel(processName, ChanTypeEnum.list)
        def rvs = extractProcDefParts(starting)
        network += rvs[0] + "\n"
        network += "    inputList: ${currentInChanName}InList,\n"
        network += "    outputAny: ${currentOutChanName}.out(),\n"
        copyProcProperties(rvs, starting, ending)
        rvs = nextProcSpan(ending + 2)
        preNetwork = preNetwork + "def $currentOutChanName = Channel.any2any()\n"
        swapChannelNames(ChanTypeEnum.any)
    }

    def ListGroupCollect = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
        confirmChannel(processName, ChanTypeEnum.list)
        def rvs = extractProcDefParts(starting)
        network += rvs[0] + "\n"
        network += "    inputList: ${currentInChanName}InList,\n"
        if (logging) network += logChanAdd
        network += "    // no output channel required\n"
        copyProcProperties(rvs, starting, ending)
    }

    def ListGroupList = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
        confirmChannel(processName, ChanTypeEnum.list)
        def rvs = extractProcDefParts(starting)
        network += rvs[0] + "\n"
        network += "    inputList: ${currentInChanName}InList,\n"
        network += "    outputList: ${currentOutChanName}OutList,\n"
        copyProcProperties(rvs, starting, ending)
        rvs = nextProcSpan(ending + 2)
//		println "LGL: $rvs"
        if (rvs[1] > rvs[0] ) {
            String returnedChanSize = scanChanSize(rvs)
            if (returnedChanSize != null) chanSize = returnedChanSize
        }
        preNetwork = preNetwork + "def $currentOutChanName = Channel.one2oneArray($chanSize)\n"
        preNetwork = preNetwork + "def ${currentOutChanName}OutList = new ChannelOutputList($currentOutChanName)\n"
        preNetwork = preNetwork + "def ${currentOutChanName}InList = new ChannelInputList($currentOutChanName)\n"
        swapChannelNames(ChanTypeEnum.list)

        //SH added
        if (logging) {
            network += "\n    //gppVis command\n";
            String lineA = inText[starting + 1] //contains variable for number of workers
            String[] wordsA = lineA.split(" ")
            String lineB = inText[starting + 2] //contains logPhaseName
            String[] wordsB = lineB.split("\"")
            network += "    Visualiser.hb.getChildren().add(Visualiser.p.addWorkers(" +wordsA[1]+  " \""+wordsB[1]+"\"));\n";
        }
    }

    def ListOneMapManyList = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
        confirmChannel(processName, ChanTypeEnum.list)
        def rvs = extractProcDefParts(starting)
        network += rvs[0] + "\n"
        network += "    inputList: ${currentInChanName}InList,\n"
        network += "    outputList: ${currentOutChanName}OutList,\n"
        copyProcProperties(rvs, starting, ending)
        rvs = nextProcSpan(ending + 2)
        String returnedChanSize = scanChanSize(rvs)
        if (returnedChanSize != null) chanSize = returnedChanSize
        preNetwork = preNetwork + "def $currentOutChanName = Channel.one2oneArray($chanSize)\n"
        preNetwork = preNetwork + "def ${currentOutChanName}OutList = new ChannelOutputList($currentOutChanName)\n"
        preNetwork = preNetwork + "def ${currentOutChanName}InList = new ChannelInputList($currentOutChanName)\n"
        swapChannelNames(ChanTypeEnum.list)
    }

    def ListOneMapOneList = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
        ListGroupList(processName, starting, ending)
    }

    def ListReduceList = { String processName, int starting, int ending ->
//			println "$processName: $starting, $ending"
        confirmChannel(processName, ChanTypeEnum.list)
        def rvs = extractProcDefParts(starting)
        network += rvs[0] + "\n"
        network += "    inputList: ${currentInChanName}InList,\n"
        network += "    outputList: ${currentOutChanName}OutList,\n"
        copyProcProperties(rvs, starting, ending)
        preNetwork = preNetwork + "def $currentOutChanName = Channel.one2oneArray($chanSize)\n"
        preNetwork = preNetwork + "def ${currentOutChanName}OutList = new ChannelOutputList($currentOutChanName)\n"
        preNetwork = preNetwork + "def ${currentOutChanName}InList = new ChannelInputList($currentOutChanName)\n"
        swapChannelNames(ChanTypeEnum.list)
    }

    def ListThreePhaseWorkerList = { String processName, int starting, int ending ->
//			println "$processName: $starting, $ending"
        confirmChannel(processName, ChanTypeEnum.list)
        def rvs = extractProcDefParts(starting)
        network += rvs[0] + "\n"
        network += "    inputList: ${currentInChanName}InList,\n"
        network += "    outputList: ${currentOutChanName}OutList,\n"
        copyProcProperties(rvs, starting, ending)
        rvs = nextProcSpan(ending + 2)
        preNetwork = preNetwork + "def $currentOutChanName = Channel.one2oneArray($chanSize)\n"
        String returnedChanSize = scanChanSize(rvs)
        if (returnedChanSize != null) chanSize = returnedChanSize
        preNetwork = preNetwork + "def ${currentOutChanName}OutList = new ChannelOutputList($currentOutChanName)\n"
        preNetwork = preNetwork + "def ${currentOutChanName}InList = new ChannelInputList($currentOutChanName)\n"
        swapChannelNames(ChanTypeEnum.list)
    }

//matrix
    def MultiCoreEngine = { String processName, int starting, int ending ->
//      println "$processName: $starting, $ending"
        confirmChannel(processName, ChanTypeEnum.one)
        def rvs = extractProcDefParts(starting)
        network += rvs[0] + "\n"
        network += "    input: ${currentInChanName}.in(),\n"
        network += "    output: ${currentOutChanName}.out(),\n"
        copyProcProperties(rvs, starting, ending)
        preNetwork = preNetwork + "def $currentOutChanName = Channel.one2one()\n"
        swapChannelNames(ChanTypeEnum.one)
    }

    def ImageEngine = { String processName, int starting, int ending ->
        MultiCoreEngine(processName, starting, ending)
    }

// pipelines
    def OnePipelineCollect = { String processName, int starting, int ending ->
        println "$processName: $starting, $ending"
        Collect	(processName, starting , ending)
    }

    def OnePipelineOne = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
        confirmChannel(processName, ChanTypeEnum.one)
        def rvs = extractProcDefParts(starting)
        network += rvs[0] + "\n"
        network += "    input: ${currentInChanName}.in(),\n"
        network += "    output: ${currentOutChanName}.out(),\n"
        copyProcProperties(rvs, starting, ending)
        preNetwork = preNetwork + "def $currentOutChanName = Channel.one2one()\n"
        swapChannelNames(ChanTypeEnum.one)
    }

// terminals
    def Collect = { String processName, int starting, int ending ->
//			println "$processName: $starting, $ending"
        confirmChannel(processName, ChanTypeEnum.one)
        def rvs = extractProcDefParts(starting)
        network += rvs[0] + "\n"
        network += "    input: ${currentInChanName}.in(),\n"
        if (logging) network += logChanAdd
        network += "    // no output channel required\n"
        copyProcProperties(rvs, starting, ending)

        //SH added
        if (logging){
            network += "\n    //gppVis command\n";
            String line = inText[starting+1]
            String[] words = line.split("\"")
            network += "    Visualiser.hb.getChildren().add(Visualiser.p.populateMap(\""+words[1]+"\"));\n";
        }
    }

    def CollectUI = { String processName, int starting, int ending ->
//			println "$processName: $starting, $ending"
        Collect	(processName, starting , ending)
    }

    def Emit = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
        def rvs = extractProcDefParts(starting)
        network += rvs[0] + "\n"
        network += "    // input channel not required\n"
        network += "    output: ${currentOutChanName}.out(),\n"
        copyProcProperties(rvs, starting, ending)
        preNetwork = preNetwork + "def $currentOutChanName = Channel.one2one()\n"
        swapChannelNames(ChanTypeEnum.one)

        //SH added
        if (logging){
            network += "\n    //gppVis command\n";
            String line = inText[starting+1]
            String[] words = line.split("\"")
            network += "    Visualiser.hb.getChildren().add(Visualiser.p.populateMap(\""+words[1]+"\"));\n";
        }
    }

    def EmitFromInput = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
        confirmChannel(processName, ChanTypeEnum.one)
        def rvs = extractProcDefParts(starting)
        network += rvs[0] + "\n"
        network += "    input: ${currentInChanName}.in(),\n"
        network += "    output: ${currentOutChanName}.out(),\n"
        copyProcProperties(rvs, starting, ending)
        preNetwork = preNetwork + "def $currentOutChanName = Channel.one2one()\n"
        swapChannelNames(ChanTypeEnum.one)

        //SH added
        if (logging){
            network += "\n    //gppVis command\n";
            String line = inText[starting+1]
            String[] words = line.split("\"")
            network += "    Visualiser.hb.getChildren().add(Visualiser.p.populateMap(\""+words[1]+"\"));\n";
        }
    }

    def EmitWithFeedback = {String processName, int starting, int ending ->
        // assumes there is only one feedback loop in the network!!
        // the feedback channel is named explicitly
//		println "$processName: $starting, $ending"
//		network += inText[starting]
        def rvs = extractProcDefParts(starting)
        network += rvs[0] + "\n"
        network += "    feedback: feedbackChan.in(),\n"
        network += "    output: ${currentOutChanName}.out(),\n"
        copyProcProperties(rvs, starting, ending)
        preNetwork = preNetwork + "def feedbackChan = Channel.one2one()\n"
        preNetwork = preNetwork + "def $currentOutChanName = Channel.one2one()\n"
        swapChannelNames(ChanTypeEnum.one)

        //SH added
        if (logging){
            network += "\n    //gppVis command\n";
            String line = inText[starting+2]
            String[] words = line.split("\"")
            network += "    Visualiser.hb.getChildren().add(Visualiser.p.populateMap(\""+words[1]+"\"));\n";
        }
    }

    def EmitWithLocal = {String processName, int starting, int ending ->
//			println "$processName: $starting, $ending"
        Emit(processName, starting, ending)
//		def rvs = extractProcDefParts(starting)
//		network += rvs[0] + "\n"
//		network += "    // input channel not required\n"
//		network += "    output: ${currentOutChanName}.out(),\n"
//		copyProcProperties(rvs, starting, ending)
//		preNetwork = preNetwork + "def $currentOutChanName = Channel.one2one()\n"
//		swapChannelNames(ChanTypeEnum.one)
    }

    def TestPoint = { String processName, int starting, int ending ->
        println "$processName: $starting, $ending"
    }

// transformers
    def CombineNto1 = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
        confirmChannel(processName, ChanTypeEnum.one)
        def rvs = extractProcDefParts(starting)
        network += rvs[0] + "\n"
        network += "    input: ${currentInChanName}.in(),\n"
        network += "    output: ${currentOutChanName}.out(),\n"
        copyProcProperties(rvs, starting, ending)
        preNetwork = preNetwork + "def $currentOutChanName = Channel.one2one()\n"
        swapChannelNames(ChanTypeEnum.one)

        //SH added
        if (logging){
            network += "\n    //gppVis command\n";
            String line = inText[starting+3]
            String[] words = line.split("\"")
            network += "    Visualiser.hb.getChildren().add(Visualiser.p.populateMap(\""+words[1]+"\"));\n";
        }
    }

    def FeedbackBool = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
        confirmChannel(processName, ChanTypeEnum.one)
        def rvs = extractProcDefParts(starting)
        network += rvs[0] + "\n"
        network += "    input: ${currentInChanName}.in(),\n"
        network += "    output: ${currentOutChanName}.out(),\n"
        network += "    feedback: feedbackChan.out(),\n"
        copyProcProperties(rvs, starting, ending)
        preNetwork = preNetwork + "def $currentOutChanName = Channel.one2one()\n"
        swapChannelNames(ChanTypeEnum.one)

        //SH added
        if (logging){
            network += "\n    //gppVis command\n";
            String line = inText[starting+2]
            String[] words = line.split("\"")
            network += "    Visualiser.hb.getChildren().add(Visualiser.p.populateMap(\""+words[1]+"\"));\n";
        }
    }

    def FeedbackObject = { String processName, int starting, int ending ->
        println "$processName: $starting, $ending"
    }

// workers
    def ThreePhaseWorker = {  String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
        confirmChannel(processName, ChanTypeEnum.one)
        def rvs = extractProcDefParts(starting)
        network += rvs[0] + "\n"
        network += "    input: ${currentInChanName}.in(),\n"
        network += "    output: ${currentOutChanName}.out(),\n"
        copyProcProperties(rvs, starting, ending)
        preNetwork = preNetwork + "def $currentOutChanName = Channel.one2one()\n"
        swapChannelNames(ChanTypeEnum.one)
    }

    def Worker = {  String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
        confirmChannel(processName, ChanTypeEnum.one)
        def rvs = extractProcDefParts(starting)
        network += rvs[0] + "\n"
        network += "    input: ${currentInChanName}.in(),\n"
        network += "    output: ${currentOutChanName}.out(),\n"
        copyProcProperties(rvs, starting, ending)
        preNetwork = preNetwork + "def $currentOutChanName = Channel.one2one()\n"
        swapChannelNames(ChanTypeEnum.one)
    }

// closures used in file processing
    def processImports = {
        while (inText[currentLine] == " "){
            outText << inText[currentLine] + "\n"
            currentLine ++
        }
        while (inText[currentLine].startsWith("import") ||
                inText[currentLine] == " "){
            outText << inText[currentLine] + "\n"
            currentLine ++
        }
    }

    def processLogDetails = {
        List logTokens = inText[currentLine].tokenize()
        String collectors = logTokens[1]
        logFileName = logTokens[2]
        logging = true
        preNetwork += "\nimport gppLibrary.Logger\n"
        preNetwork += "import gppLibrary.LoggingVisualiser\n"
        preNetwork += "import gppLibrary.gppVis.Visualiser\n"
        preNetwork += "import gppLibrary.gppVis.Connector\n"
        preNetwork += "import javafx.application.Platform\n\n"

        preNetwork += "def logChan = Channel.any2one()\n"
        preNetwork += "Logger.initLogChannel(logChan.out())\n"
        preNetwork += "def logVis = new LoggingVisualiser ( logInput: logChan.in(), \n"
        preNetwork += "                     collectors: $collectors,\n"
        preNetwork += "                     logFileName: $logFileName )\n\n"
        processNames << "logVis"

        //SH added
        preNetwork += "//gppVis command\n"
        preNetwork += "new Thread() {\n"
        preNetwork += "	@Override\n"
        preNetwork += "	public void run() {\n"
        preNetwork += "		Visualiser.main();\n"
        preNetwork += "	}\n"
        preNetwork += "}.start();\n"
    }

    def processPreNetwork = {
        boolean startProcess = ( (inText[currentLine] =~ /Emit/) ||
                (inText[currentLine] =~ /Parallel/) )
        while ( ! startProcess){
            preNetwork += inText[currentLine] + "\n"
            // added to deal with logging
            if (inText[currentLine].startsWith("//@log")) processLogDetails()
            currentLine ++
            startProcess = ( (inText[currentLine] =~ /Emit/) ||
                    (inText[currentLine] =~ /Parallel/) )
        }
        preNetwork = preNetwork + "\n//NETWORK\n\n"
    }

    def processPostNetwork = {

        //SH added
        if (logging){
            postNetwork += "//gppVis command\n"
            postNetwork += "//short delay to give JavaFx time to start up.\n"
            postNetwork += "sleep(2000)\n";
            postNetwork += "Platform.runLater(new Runnable() {\n"
            postNetwork += "	@Override\n"
            postNetwork += "	void run() {\n"
            postNetwork += "		Visualiser.networkScene()\n"
            postNetwork += "	}\n"
            postNetwork += "});\n"

            postNetwork += "\n//short delay to give JavaFx time to display.\n"
            postNetwork += "sleep(3000);\n\n"
        }

        if ( !pattern) postNetwork += "PAR network = new PAR()\n network = new PAR($processNames)\n network.run()\n network.removeAllProcesses()"
        else postNetwork += "${processNames[0]}.run()\n"
        postNetwork += "\n//END\n\n"

        //SH added
        if (logging) {
            postNetwork += "//gppVis command\n"
            postNetwork += "//Now that the network has completed, tell the vis where the log file is so it\n"
            postNetwork += "//can access the data so it can replay it.\n"
            postNetwork += "Platform.runLater(new Runnable() {\n"
            postNetwork += "	@Override\n"
            postNetwork += "	void run() {\n"
            postNetwork += "		Visualiser.readLog(\""+logFileName.replace("\"","")+"log.csv\")\n"
            postNetwork += "	}\n"
            postNetwork += "});\n"
        }


        while (currentLine < inText.size()){
            postNetwork += inText[currentLine] + "\n"
            currentLine ++
        }
    }

}
