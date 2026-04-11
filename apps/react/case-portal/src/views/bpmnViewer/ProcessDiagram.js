import { ReactBpmn } from './BpmnReact'

export const ProcessDiagram = ({ processDefinitionId, activityInstances }) => {
  return (
    <ReactBpmn
      processDefinitionId={processDefinitionId}
      activities={activityInstances}
    />
  )
}
