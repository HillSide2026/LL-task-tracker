import { FormApi, getApiBaseUrl } from '../api'

export const FormService = {
  getAll,
  getByKey,
  getVariableById,
  update,
  remove,
  create,
}

async function create(keycloak, body) {
  try {
    return FormApi.createForm(keycloak, body)
  } catch (err) {
    console.log(err)
    return await Promise.reject(err)
  }
}

async function update(keycloak, id, body) {
  try {
    return FormApi.updateForm(keycloak, id, body)
  } catch (err) {
    console.log(err)
    return await Promise.reject(err)
  }
}

async function remove(keycloak, id) {
  try {
    return FormApi.deleteForm(keycloak, id)
  } catch (err) {
    console.log(err)
    return await Promise.reject(err)
  }
}

async function getAll(keycloak) {
  try {
    return FormApi.findForms(keycloak)
  } catch (err) {
    console.log(err)
    return await Promise.reject(err)
  }
}

async function getByKey(keycloak, formKey) {
  try {
    const requested = await FormApi.getForm(keycloak, formKey)
    const data = requestRemoteDataSourceAndFillRecordTypesIfRequired(
      requested,
      keycloak,
    )

    return Promise.resolve(data)
  } catch (err) {
    console.log(err)
    return await Promise.reject(err)
  }
}

async function getVariableById(keycloak, processInstanceId) {
  try {
    return FormApi.findVariables(keycloak, processInstanceId)
  } catch (err) {
    console.log(err)
    return await Promise.reject(err)
  }
}

function requestRemoteDataSourceAndFillRecordTypesIfRequired(
  original,
  keycloak,
) {
  function processComponentWithContext(components) {
    const apiBaseUrl = getApiBaseUrl()

    return components?.map((item) => {
      if (item.type === 'recordtype') {
        const options = item.customOptions
        const typeRender =
          options.inputType === 'selectone' ? 'select' : 'selectboxes'
        const template = options.template
        const recordId = options.recordType.id
        const valueProperty = options.valueProperty

        return {
          ...item,
          type: typeRender,
          dataSrc: 'url',
          template: `<span>${template}</span>`,
          valueProperty: valueProperty,
          data: {
            url: `${apiBaseUrl}/record/${recordId}`,
            headers: [
              {
                key: 'Authorization',
                value: `Bearer ${keycloak.token}`,
              },
            ],
          },
        }
      }

      // Handle components recursively
      if (item.components) {
        return {
          ...item,
          components: processComponentWithContext(item.components),
        }
      }

      // Handle columns (each column has a "components" array)
      if (item.columns) {
        return {
          ...item,
          columns: item.columns.map((column) => ({
            ...column,
            components: processComponentWithContext(column.components || []),
          })),
        }
      }

      return item
    })
  }

  return {
    ...original,
    structure: {
      ...original.structure,
      components: processComponentWithContext(original.structure?.components),
    },
  }
}
