import { useCallback, useEffect, useRef } from 'react'
import BpmnJS from 'bpmn-js/dist/bpmn-navigated-viewer.production.min.js'
import './BpmnIo.css'
import { useSession } from 'SessionStoreContext'
import { ProcessDefService } from 'services/ProcessDefService'

export const ReactBpmn = ({ processDefinitionId, activities }) => {
  const containerRef = useRef(null)
  const keycloak = useSession()

  const memoizedCallback = useCallback(
    (text) => {
      const container = containerRef.current
      const bpmnViewer = new BpmnJS({ container })
      bpmnViewer.importXML(text).then(() => {
        const canvas = bpmnViewer.get('canvas')
        activities.forEach((activity) =>
          canvas.addMarker(activity.activityId, 'highlight'),
        )
        canvas.zoom('fit-viewport')
      })
    },

    [activities],
  )

  useEffect(() => {
    ProcessDefService.getBPMNXml(keycloak, processDefinitionId)
      .then((text) => {
        memoizedCallback(text)
      })
      .catch((err) => console.log(err))
  }, [keycloak, memoizedCallback, processDefinitionId])

  const Div = useCallback(
    ({ containerRef }) => {
      return (
        <div
          style={{
            height: 500,
            padding: '10px',
            margin: '10px',
            border: '1px solid rgba(0, 0, 0, 0.05)',
          }}
          className='react-bpmn-diagram-container'
          ref={containerRef}
        ></div>
      )
    },

    [activities],
  )

  return <Div containerRef={containerRef} />
}
