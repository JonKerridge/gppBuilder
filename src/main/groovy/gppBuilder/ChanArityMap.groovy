package gppBuilder;

/**
 * ChanArityMap provides a map of the processes to their PCA enumeration
 *
 */
class ChanArityMap {
	static Map arityMap = [ "Emit" : PCA.NoneOne,
						    "EmitWithLocal" : PCA.NoneOne,
							
							"ListGroupCollect" : PCA.ListNone,
							
							"Collect" : PCA.OneNone,
							"OnePipelineCollect" : PCA.OneNone,
							"CollectUI" : PCA.OneNone,
							"TestPoint" : PCA.OneNone,
							
							"GroupOfPipelineCollects" : PCA.AnyNone,
							"PipelineOfGroupCollects" : PCA.AnyNone,
							
							"EmitWithFeedback" : PCA.NoneFbackOne,
							 	
							"AnyFanAny" : PCA.AnyAny,
							"AnySeqCastAny" : PCA.AnyAny,
							
							"PipelineOfGroups" : PCA.AnyAnyProps,
							"AnyGroupAny" : PCA.AnyAnyProps,
							
							"AnyGroupList" : PCA.AnyList,
							
							"AnyFanOne" : PCA.AnyOne,
								
							"OneFanAny" : PCA.OneAny,
							"OneFanRequestedAny" : PCA.OneAnyAny,
							"OneSeqCastAny" : PCA.OneAny,
							"Node" : PCA.OneAny,
							
							"OneDirectedList" : PCA.OneListProps,
							"OneIndexedList" : PCA.OneListProps,
							 
							"OneFanList" : PCA.OneList,
							"OneParCastList" : PCA.OneList,
							"OneSeqCastList" : PCA.OneList,
							
							"BasicDandC" : PCA.OneOne,
							"Mapper" : PCA.OneOne,
							"OnePipelineOne" : PCA.OneOne,
							"EmitFromInput" : PCA.OneOne,
							"CombineNto1" : PCA.OneOne,
							"ThreePhaseWorker" : PCA.OneOne,
							"WorkerTerminating" : PCA.OneOne,
								
							"ListGroupAny" : PCA.ListAny,
							
							"GroupOfPipelines" : PCA.ListList,
							"ListGroupList" : PCA.ListList,
							"ListMapList" : PCA.ListList,
							"ListReduceList" : PCA.ListNEList,
							"ListOneMapManyList": PCA.ListNEList,
							"ListThreePhaseWorkerList" : PCA.ListList,
							
							"ListFanOne" : PCA.ListOne,
							"ListParOne" : PCA.ListOne,
							"ListSeqOne" : PCA.ListOne,
							"N_WayMerge" : PCA.ListOneProps,
							"Reducer" : PCA.ListOneProps,
								
							"Root" : PCA.OneFbackOneOne,
							"FeedbackBool" : PCA.OneFbackOne,
							"FeedbackObject" : PCA.OneFbackOne,
								
							"RequestingFanAny" : PCA.RequestAny,
							"RequestingSeqCastAny" : PCA.RequestAny,
							"NodeRequestingFanAny" : PCA.RequestAny,
							"NodeRequestingSeqCastAny" : PCA.RequestAny,
							
							"RequestingFanList" : PCA.RequestList,
							"RequestParCastList" : PCA.RequestList,
							"RequestSeqCastList" : PCA.RequestList,
							"NodeRequestingFanList" : PCA.RequestList,
							"NodeRequestParCastList" : PCA.RequestList,
							"NodeRequestSeqCastList" : PCA.RequestList,
							
							"OneNodeRequestedList" : PCA.ListRequested
							
						]
	
	
	}
