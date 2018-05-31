package gppBuilder;

/**
 * ChanTypeEnum enumerates the different types of communication that a GPP process can use.
 *
 */
enum ChanTypeEnum {
    one,
    any,
    list}


/**
 * PCA is an enumeration of the combinations of communication type used by each process in
 * the GPP library
 *
 */
enum PCA {				// ProcessChannelArity
	NoneOne,			// Emit, EmitWithLocal
	ListNone,			// ListGroupCollect
	OneNone, 			// Collect, OnePipelineCollect, CollectUI, TestPoint
	AnyNone,			// GroupOfPipelineCollects, PipelineOfGroupCollects
	NoneFbackOne,		// EmitWithFeedback
	
	AnyAny,				// AnyFanAny, AnySeqCastAny
	AnyAnyProps,		// PipelineOfGroups, AnyGroupAny
	AnyList,			// AnyGroupList
	AnyOne,				// AnyFanOne 
	
	OneAny,				// OneFanAny, OneSeqCastAny, Node
	OneAnyAny,			//  OneFanRequestedAny
	OneList,			// OneFanList, OneParCastList, OneSeqCastList
	OneListProps,		// OneDirectedList, OneIndexedList, these processes have properties
	OneOne,				// BasicDandC, Mapper, OnePipelineOne, EmitFromInput, CombineNto1, ThreePhaseWorker, WorkerTerminating
	
	ListAny,			// ListGroupAny
	ListList,			// GroupOfPipelines, ListGroupList, ListMapList, ListThreePhaseWorkerList
	ListNEList,			// ListReduceList, ListOneMapManyList
	ListOne,			// ListFanOne, ListParOne, ListSeqOne 
	ListOneProps,		// NWayMerge, Reducer
	
	OneFbackOneOne,		// Root
	OneFbackOne,		// FeedbackBool, FeedbackObject
	
	RequestAny,			// RequestingFanAny, RequestingSeqCastAny, NodeRequestingFanAny, NodeRequestingSeqCastAny
	RequestList,		// RequestingFanList, RequestParCastList, RequestSeqCastList, NodeRequestingFanList, NodeRequestParCastList, NodeRequestSeqCastList
	ListRequested		// OneNodeRequestedList
}

